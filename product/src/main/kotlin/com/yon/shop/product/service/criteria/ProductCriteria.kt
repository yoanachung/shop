package com.yon.shop.product.service.criteria

import com.yon.shop.product.domain.enumeration.SalesStatus
import org.springdoc.api.annotations.ParameterObject
import tech.jhipster.service.Criteria
import tech.jhipster.service.filter.BigDecimalFilter
import tech.jhipster.service.filter.Filter
import tech.jhipster.service.filter.IntegerFilter
import tech.jhipster.service.filter.LongFilter
import tech.jhipster.service.filter.StringFilter
import java.io.Serializable

/**
 * Criteria class for the [com.yon.shop.product.domain.Product] entity. This class is used in
 * [com.yon.shop.product.web.rest.ProductResource] to receive all the possible filtering options from the
 * Http GET request parameters.
 * For example the following could be a valid request:
 * ```/products?id.greaterThan=5&attr1.contains=something&attr2.specified=false```
 * As Spring is unable to properly convert the types, unless specific [Filter] class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
data class ProductCriteria(
    var id: LongFilter? = null,
    var name: StringFilter? = null,
    var description: StringFilter? = null,
    var price: BigDecimalFilter? = null,
    var remainingCount: IntegerFilter? = null,
    var status: SalesStatusFilter? = null,
    var distinct: Boolean? = null
) : Serializable, Criteria {

    constructor(other: ProductCriteria) :
        this(
            other.id?.copy(),
            other.name?.copy(),
            other.description?.copy(),
            other.price?.copy(),
            other.remainingCount?.copy(),
            other.status?.copy(),
            other.distinct
        )

    /**
     * Class for filtering SalesStatus
     */
    class SalesStatusFilter : Filter<SalesStatus> {
        constructor()

        constructor(filter: SalesStatusFilter) : super(filter)

        override fun copy() = SalesStatusFilter(this)
    }

    override fun copy() = ProductCriteria(this)

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
