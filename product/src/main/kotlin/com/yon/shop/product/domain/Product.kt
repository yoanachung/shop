package com.yon.shop.product.domain

import com.yon.shop.product.domain.enumeration.SalesStatus
import java.math.BigDecimal
import javax.persistence.*

@Entity
data class Product(
    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "price", precision = 21, scale = 2, nullable = false)
    var price: BigDecimal,

    @Column(name = "remaining_count")
    var remainingCount: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: SalesStatus,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
}
