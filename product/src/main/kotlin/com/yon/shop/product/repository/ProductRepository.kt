package com.yon.shop.product.repository

import com.yon.shop.product.domain.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

/**
 * Spring Data SQL repository for the [Product] entity.
 */
@Repository
interface ProductRepository : JpaRepository<Product, Long>, JpaSpecificationExecutor<Product>
