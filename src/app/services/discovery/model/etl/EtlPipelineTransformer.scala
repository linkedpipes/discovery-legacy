package services.discovery.model.etl

import services.discovery.components.analyzer.{EtlRdf2File, EtlSparqlGraphProtocol}
import services.discovery.components.datasource.{EtlSparqlEndpoint, JenaDataSource, SparqlEndpoint}
import services.discovery.model.components.{SparqlEndpointInstance, SparqlConstructExtractorInstance, UnionInstance, ApplicationInstance}
import services.discovery.model.{Pipeline, PipelineComponent, PortBinding, GuidGenerator}

class EtlPipelineTransformer(pipeline: Pipeline) {

  def transform: Pipeline = rules.foldLeft(pipeline)(applyRule)

  private val extractorRule = TransformationRule(
    "Extractor",
    new PipelineFragmentMatcher(
      b => {
        val endsWithExtractors = b.filter(_.endComponent.componentInstance.isInstanceOf[SparqlConstructExtractorInstance])
        val startsWithSparqlEndpoint = endsWithExtractors.filter(_.startComponent.componentInstance.isInstanceOf[SparqlEndpointInstance])
        val startsWithExtractor = b.filter(_.startComponent.componentInstance.isInstanceOf[SparqlConstructExtractorInstance])

        startsWithSparqlEndpoint.map { se =>
          Seq(se) ++ startsWithExtractor.filter(_.startComponent == se.endComponent)
        }
      }
    ),
    (p, b) => {
      val mainBinding = b.find(_.startComponent.componentInstance.isInstanceOf[SparqlEndpointInstance]).get
      val otherBinding = b.filterNot(_ == mainBinding).head
      val ds = mainBinding.startComponent.componentInstance.asInstanceOf[SparqlEndpointInstance]
      val ex = mainBinding.endComponent.componentInstance.asInstanceOf[SparqlConstructExtractorInstance]

      val etlDs = EtlSparqlEndpoint(ds.url, ds.defaultGraphIris, ex.getQueryByPort(mainBinding.endPort), ds.label)
      val c = PipelineComponent(GuidGenerator.next, etlDs, mainBinding.startComponent.discoveryIteration)

      val newBinding = PortBinding(c, otherBinding.endPort, otherBinding.endComponent)

      val newB = newBindings(p.bindings, b, Seq(newBinding))
      val newC = newComponents(p.components, Seq(mainBinding.startComponent, mainBinding.endComponent), Seq(c))

      p.copy(bindings = newB, components = newC)
    }
  )

  private val unionRule = TransformationRule(
    "Union",
    new PipelineFragmentMatcher(
      b => {
        val endsWithUnion = b.filter(_.endComponent.componentInstance.isInstanceOf[UnionInstance])
        val startsWithUnion = b.filter(_.startComponent.componentInstance.isInstanceOf[UnionInstance])

        startsWithUnion.map { su =>
          Seq(su) ++ endsWithUnion.filter(_.endComponent == su.startComponent)
        }
      }
    ),
    (p, b) => {
      val unionOutputBinding = b.find(_.startComponent.componentInstance.isInstanceOf[UnionInstance]).get
      val unionInputBindings = b.filter(_.endComponent.componentInstance.isInstanceOf[UnionInstance])

      val bindingsToAdd = unionInputBindings.map { ub =>
        PortBinding(ub.startComponent, unionOutputBinding.endPort, unionOutputBinding.endComponent)
      }

      val newB = newBindings(p.bindings, b, bindingsToAdd)
      val newC = newComponents(p.components, Seq(unionOutputBinding.startComponent), Seq())
      p.copy(bindings = newB, components = newC)
    }
  )

  private val visualizerRule = TransformationRule(
    "Application",
    new PipelineFragmentMatcher(
      b => Seq(b.filter(_.endComponent.componentInstance.isInstanceOf[ApplicationInstance]))
    ),
    (p, b) => {
      val iteration = b.head.endComponent.discoveryIteration
      val lpRdf2File = EtlRdf2File()
      val rdf2File = PipelineComponent("rdf2file", lpRdf2File, iteration)
      val lpSparqlGraphStore = EtlSparqlGraphProtocol(b.head.endComponent.componentInstance.label)
      val sparqlGraphStore = PipelineComponent("sparqlGraphStore", lpSparqlGraphStore, iteration)

      val bindingsToAdd = Seq(
        PortBinding(b.head.startComponent, lpRdf2File.port, rdf2File),
        PortBinding(rdf2File, lpSparqlGraphStore.port, sparqlGraphStore)
      )

      val newC = newComponents(p.components, b.map(_.endComponent), Seq(rdf2File, sparqlGraphStore))
      val newB = newBindings(p.bindings, b, bindingsToAdd)

      p.copy(bindings = newB, components = newC, lastComponent = sparqlGraphStore)
    }
  )

  private val jenaDataSourceRule = TransformationRule(
    "Jena DS",
    new PipelineFragmentMatcher(
      b => Seq(b.filter(_.startComponent.componentInstance.isInstanceOf[JenaDataSource]))
    ),
    (p, b) => {
      b.foldLeft(p)((pip, binding) => {
        val jenaSource = binding.startComponent.componentInstance.asInstanceOf[JenaDataSource]
        val urn = GuidGenerator.nextUrn
        val endpointInstance = new SparqlEndpoint(
          s"https://linked.opendata.cz/ontology/datasource/$urn",
          "http://example.com",
          Seq(urn),
          label = jenaSource.label
        )
        val newPipelineComponent = binding.startComponent.copy(componentInstance = endpointInstance)
        val newC = newComponents(pip.components, Seq(binding.startComponent), Seq(newPipelineComponent))
        val newB = newBindings(pip.bindings, Seq(binding), Seq(PortBinding(newPipelineComponent, binding.endPort, binding.endComponent)))

        pip.copy(components = newC, bindings = newB)
      })
    }
  )

  // single pass!
  private val rules = Seq(
    jenaDataSourceRule,
    extractorRule,
    unionRule,
    visualizerRule
  )

  private def applyRule(pipeline: Pipeline, transformationRule: TransformationRule): Pipeline = {
    val fragments = transformationRule.pipelineFragmentMatcher.matchFragments(pipeline)
    fragments.filter(_.nonEmpty).foldLeft(pipeline)(transformationRule.transformer)
  }

  private def newBindings(bindings: Seq[PortBinding], bindingsToRemove: Seq[PortBinding], bindingsToAdd: Seq[PortBinding]) = {
    bindings.filterNot(bindingsToRemove.contains(_)) ++ bindingsToAdd
  }

  private def newComponents(components: Seq[PipelineComponent], componentsToRemove: Seq[PipelineComponent], componentsToAdd: Seq[PipelineComponent]) = {
    components.filterNot(componentsToRemove.contains(_)) ++ componentsToAdd
  }

}

case class TransformationRule(name: String, pipelineFragmentMatcher: PipelineFragmentMatcher, transformer: (Pipeline, Seq[PortBinding]) => Pipeline)

class PipelineFragmentMatcher(bindingMatcher: Seq[PortBinding] => Seq[Seq[PortBinding]]) {
  def matchFragments(pipeline: Pipeline): Seq[Seq[PortBinding]] = {
    bindingMatcher(pipeline.bindings)
  }
}
