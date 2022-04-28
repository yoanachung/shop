package com.yon.shop.product.service.mapper

import com.yon.shop.product.domain.Product
import com.yon.shop.product.service.dto.ProductDTO
import org.mapstruct.*

/**
 * Mapper for the entity [Product] and its DTO [ProductDTO].
 */
@Mapper(componentModel = "spring")
interface ProductMapper :
    EntityMapper<ProductDTO, Product>
