package com.yon.shop.product.service.dto

import com.yon.shop.product.domain.enumeration.SalesStatus
import java.io.Serializable
import java.math.BigDecimal
import java.util.Objects
import javax.validation.constraints.*

/**
 * A DTO for the [com.yon.shop.product.domain.Product] entity.
 */
data class ProductDTO(

    var id: Long? = null,

    @get: NotNull
    var name: String? = null,

    var description: String? = null,

    @get: NotNull
    @get: DecimalMin(value = "0")
    var price: BigDecimal? = null,

    @get: Min(value = 0)
    var remainingCount: Int? = null,

    @get: NotNull
    var status: SalesStatus? = null
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductDTO) return false
        val productDTO = other
        if (this.id == null) {
            return false
        }
        return Objects.equals(this.id, productDTO.id)
    }

    override fun hashCode() = Objects.hash(this.id)
}
