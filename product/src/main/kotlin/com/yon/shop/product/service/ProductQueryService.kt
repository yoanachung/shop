package com.yon.shop.product.service

import com.yon.shop.product.domain.* // for static metamodels
import com.yon.shop.product.domain.Product
import com.yon.shop.product.repository.ProductRepository
import com.yon.shop.product.service.criteria.ProductCriteria
import com.yon.shop.product.service.dto.ProductDTO
import com.yon.shop.product.service.mapper.ProductMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.jhipster.service.QueryService

/**
 * Service for executing complex queries for [Product] entities in the database.
 * The main input is a [ProductCriteria] which gets converted to [Specification],
 * in a way that all the filters must apply.
 * It returns a [MutableList] of [ProductDTO] or a [Page] of [ProductDTO] which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
class ProductQueryService(
    private val productRepository: ProductRepository,
    private val productMapper: ProductMapper,
) : QueryService<Product>() {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Return a [MutableList] of [ProductDTO] which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    fun findByCriteria(criteria: ProductCriteria?): MutableList<ProductDTO> {
        log.debug("find by criteria : $criteria")
        val specification = createSpecification(criteria)
        return productMapper.toDto(productRepository.findAll(specification))
    }

    /**
     * Return a [Page] of [ProductDTO] which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    fun findByCriteria(criteria: ProductCriteria?, page: Pageable): Page<ProductDTO> {
        log.debug("find by criteria : $criteria, page: $page")
        val specification = createSpecification(criteria)
        return productRepository.findAll(specification, page)
            .map(productMapper::toDto)
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    fun countByCriteria(criteria: ProductCriteria?): Long {
        log.debug("count by criteria : $criteria")
        val specification = createSpecification(criteria)
        return productRepository.count(specification)
    }

    /**
     * Function to convert [ProductCriteria] to a [Specification].
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching [Specification] of the entity.
     */
    protected fun createSpecification(criteria: ProductCriteria?): Specification<Product?> {
        var specification: Specification<Product?> = Specification.where(null)
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            val distinctCriteria = criteria.distinct
            if (distinctCriteria != null) {
                specification = specification.and(distinct(distinctCriteria))
            }
            if (criteria.id != null) {
                specification = specification.and(buildRangeSpecification(criteria.id, Product_.id))
            }
            if (criteria.name != null) {
                specification = specification.and(buildStringSpecification(criteria.name, Product_.name))
            }
            if (criteria.description != null) {
                specification = specification.and(buildStringSpecification(criteria.description, Product_.description))
            }
            if (criteria.price != null) {
                specification = specification.and(buildRangeSpecification(criteria.price, Product_.price))
            }
            if (criteria.remainingCount != null) {
                specification = specification.and(buildRangeSpecification(criteria.remainingCount, Product_.remainingCount))
            }
            if (criteria.status != null) {
                specification = specification.and(buildSpecification(criteria.status, Product_.status))
            }
        }
        return specification
    }
}
