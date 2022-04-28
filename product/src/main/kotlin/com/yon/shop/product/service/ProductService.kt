package com.yon.shop.product.service

import com.yon.shop.product.domain.Product
import com.yon.shop.product.repository.ProductRepository
import com.yon.shop.product.service.dto.ProductDTO
import com.yon.shop.product.service.mapper.ProductMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

/**
 * Service Implementation for managing [Product].
 */
@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository,
    private val productMapper: ProductMapper,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a product.
     *
     * @param productDTO the entity to save.
     * @return the persisted entity.
     */
    fun save(productDTO: ProductDTO): ProductDTO {
        log.debug("Request to save Product : $productDTO")
        var product = productMapper.toEntity(productDTO)
        product = productRepository.save(product)
        return productMapper.toDto(product)
    }

    /**
     * Update a product.
     *
     * @param productDTO the entity to save.
     * @return the persisted entity.
     */
    fun update(productDTO: ProductDTO): ProductDTO {
        log.debug("Request to save Product : {}", productDTO)
        var product = productMapper.toEntity(productDTO)
        product = productRepository.save(product)
        return productMapper.toDto(product)
    }

    /**
     * Partially updates a product.
     *
     * @param productDTO the entity to update partially.
     * @return the persisted entity.
     */
    fun partialUpdate(productDTO: ProductDTO): Optional<ProductDTO> {
        log.debug("Request to partially update Product : {}", productDTO)

        return productRepository.findById(productDTO.id)
            .map {
                productMapper.partialUpdate(it, productDTO)
                it
            }
            .map { productRepository.save(it) }
            .map { productMapper.toDto(it) }
    }

    /**
     * Get all the products.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<ProductDTO> {
        log.debug("Request to get all Products")
        return productRepository.findAll(pageable)
            .map(productMapper::toDto)
    }

    /**
     * Get one product by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    fun findOne(id: Long): Optional<ProductDTO> {
        log.debug("Request to get Product : $id")
        return productRepository.findById(id)
            .map(productMapper::toDto)
    }

    /**
     * Delete the product by id.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long) {
        log.debug("Request to delete Product : $id")

        productRepository.deleteById(id)
    }
}
