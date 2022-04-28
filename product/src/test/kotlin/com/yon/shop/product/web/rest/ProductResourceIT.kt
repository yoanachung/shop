package com.yon.shop.product.web.rest

import com.yon.shop.product.IntegrationTest
import com.yon.shop.product.domain.Product
import com.yon.shop.product.domain.enumeration.SalesStatus
import com.yon.shop.product.repository.ProductRepository
import com.yon.shop.product.service.mapper.ProductMapper
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Validator
import java.math.BigDecimal
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.EntityManager
import kotlin.test.assertNotNull

/**
 * Integration tests for the [ProductResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProductResourceIT {
    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var productMapper: ProductMapper

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var validator: Validator

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var restProductMockMvc: MockMvc

    private lateinit var product: Product

    @BeforeEach
    fun initTest() {
        product = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createProduct() {
        val databaseSizeBeforeCreate = productRepository.findAll().size
        // Create the Product
        val productDTO = productMapper.toDto(product)
        restProductMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(productDTO))
        ).andExpect(status().isCreated)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeCreate + 1)
        val testProduct = productList[productList.size - 1]

        assertThat(testProduct.name).isEqualTo(DEFAULT_NAME)
        assertThat(testProduct.description).isEqualTo(DEFAULT_DESCRIPTION)
        assertThat(testProduct.price?.stripTrailingZeros()).isEqualTo(DEFAULT_PRICE.stripTrailingZeros())
        assertThat(testProduct.remainingCount).isEqualTo(DEFAULT_REMAINING_COUNT)
        assertThat(testProduct.status).isEqualTo(DEFAULT_STATUS)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createProductWithExistingId() {
        // Create the Product with an existing ID
        product.id = 1L
        val productDTO = productMapper.toDto(product)

        val databaseSizeBeforeCreate = productRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restProductMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(productDTO))
        ).andExpect(status().isBadRequest)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkNameIsRequired() {
        val databaseSizeBeforeTest = productRepository.findAll().size
        // set the field null
        product.name = null

        // Create the Product, which fails.
        val productDTO = productMapper.toDto(product)

        restProductMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(productDTO))
        ).andExpect(status().isBadRequest)

        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeTest)
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkPriceIsRequired() {
        val databaseSizeBeforeTest = productRepository.findAll().size
        // set the field null
        product.price = null

        // Create the Product, which fails.
        val productDTO = productMapper.toDto(product)

        restProductMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(productDTO))
        ).andExpect(status().isBadRequest)

        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeTest)
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkStatusIsRequired() {
        val databaseSizeBeforeTest = productRepository.findAll().size
        // set the field null
        product.status = null

        // Create the Product, which fails.
        val productDTO = productMapper.toDto(product)

        restProductMockMvc.perform(
            post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(productDTO))
        ).andExpect(status().isBadRequest)

        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProducts() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList
        restProductMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(product.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].remainingCount").value(hasItem(DEFAULT_REMAINING_COUNT)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getProduct() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        val id = product.id
        assertNotNull(id)

        // Get the product
        restProductMockMvc.perform(get(ENTITY_API_URL_ID, product.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(product.id?.toInt()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.price").value(sameNumber(DEFAULT_PRICE)))
            .andExpect(jsonPath("$.remainingCount").value(DEFAULT_REMAINING_COUNT))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getProductsByIdFiltering() {
        // Initialize the database
        productRepository.saveAndFlush(product)
        val id = product.id

        defaultProductShouldBeFound("id.equals=$id")
        defaultProductShouldNotBeFound("id.notEquals=$id")
        defaultProductShouldBeFound("id.greaterThanOrEqual=$id")
        defaultProductShouldNotBeFound("id.greaterThan=$id")

        defaultProductShouldBeFound("id.lessThanOrEqual=$id")
        defaultProductShouldNotBeFound("id.lessThan=$id")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByNameIsEqualToSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where name equals to DEFAULT_NAME
        defaultProductShouldBeFound("name.equals=$DEFAULT_NAME")

        // Get all the productList where name equals to UPDATED_NAME
        defaultProductShouldNotBeFound("name.equals=$UPDATED_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByNameIsNotEqualToSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where name not equals to DEFAULT_NAME
        defaultProductShouldNotBeFound("name.notEquals=$DEFAULT_NAME")

        // Get all the productList where name not equals to UPDATED_NAME
        defaultProductShouldBeFound("name.notEquals=$UPDATED_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByNameIsInShouldWork() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where name in DEFAULT_NAME or UPDATED_NAME
        defaultProductShouldBeFound("name.in=$DEFAULT_NAME,$UPDATED_NAME")

        // Get all the productList where name equals to UPDATED_NAME
        defaultProductShouldNotBeFound("name.in=$UPDATED_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByNameIsNullOrNotNull() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where name is not null
        defaultProductShouldBeFound("name.specified=true")

        // Get all the productList where name is null
        defaultProductShouldNotBeFound("name.specified=false")
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByNameContainsSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where name contains DEFAULT_NAME
        defaultProductShouldBeFound("name.contains=$DEFAULT_NAME")

        // Get all the productList where name contains UPDATED_NAME
        defaultProductShouldNotBeFound("name.contains=$UPDATED_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByNameNotContainsSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where name does not contain DEFAULT_NAME
        defaultProductShouldNotBeFound("name.doesNotContain=$DEFAULT_NAME")

        // Get all the productList where name does not contain UPDATED_NAME
        defaultProductShouldBeFound("name.doesNotContain=$UPDATED_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByDescriptionIsEqualToSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where description equals to DEFAULT_DESCRIPTION
        defaultProductShouldBeFound("description.equals=$DEFAULT_DESCRIPTION")

        // Get all the productList where description equals to UPDATED_DESCRIPTION
        defaultProductShouldNotBeFound("description.equals=$UPDATED_DESCRIPTION")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByDescriptionIsNotEqualToSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where description not equals to DEFAULT_DESCRIPTION
        defaultProductShouldNotBeFound("description.notEquals=$DEFAULT_DESCRIPTION")

        // Get all the productList where description not equals to UPDATED_DESCRIPTION
        defaultProductShouldBeFound("description.notEquals=$UPDATED_DESCRIPTION")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByDescriptionIsInShouldWork() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where description in DEFAULT_DESCRIPTION or UPDATED_DESCRIPTION
        defaultProductShouldBeFound("description.in=$DEFAULT_DESCRIPTION,$UPDATED_DESCRIPTION")

        // Get all the productList where description equals to UPDATED_DESCRIPTION
        defaultProductShouldNotBeFound("description.in=$UPDATED_DESCRIPTION")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByDescriptionIsNullOrNotNull() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where description is not null
        defaultProductShouldBeFound("description.specified=true")

        // Get all the productList where description is null
        defaultProductShouldNotBeFound("description.specified=false")
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByDescriptionContainsSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where description contains DEFAULT_DESCRIPTION
        defaultProductShouldBeFound("description.contains=$DEFAULT_DESCRIPTION")

        // Get all the productList where description contains UPDATED_DESCRIPTION
        defaultProductShouldNotBeFound("description.contains=$UPDATED_DESCRIPTION")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByDescriptionNotContainsSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where description does not contain DEFAULT_DESCRIPTION
        defaultProductShouldNotBeFound("description.doesNotContain=$DEFAULT_DESCRIPTION")

        // Get all the productList where description does not contain UPDATED_DESCRIPTION
        defaultProductShouldBeFound("description.doesNotContain=$UPDATED_DESCRIPTION")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByPriceIsEqualToSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where price equals to DEFAULT_PRICE
        defaultProductShouldBeFound("price.equals=$DEFAULT_PRICE")

        // Get all the productList where price equals to UPDATED_PRICE
        defaultProductShouldNotBeFound("price.equals=$UPDATED_PRICE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByPriceIsNotEqualToSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where price not equals to DEFAULT_PRICE
        defaultProductShouldNotBeFound("price.notEquals=$DEFAULT_PRICE")

        // Get all the productList where price not equals to UPDATED_PRICE
        defaultProductShouldBeFound("price.notEquals=$UPDATED_PRICE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByPriceIsInShouldWork() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where price in DEFAULT_PRICE or UPDATED_PRICE
        defaultProductShouldBeFound("price.in=$DEFAULT_PRICE,$UPDATED_PRICE")

        // Get all the productList where price equals to UPDATED_PRICE
        defaultProductShouldNotBeFound("price.in=$UPDATED_PRICE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByPriceIsNullOrNotNull() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where price is not null
        defaultProductShouldBeFound("price.specified=true")

        // Get all the productList where price is null
        defaultProductShouldNotBeFound("price.specified=false")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByPriceIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where price is greater than or equal to DEFAULT_PRICE
        defaultProductShouldBeFound("price.greaterThanOrEqual=$DEFAULT_PRICE")

        // Get all the productList where price is greater than or equal to UPDATED_PRICE
        defaultProductShouldNotBeFound("price.greaterThanOrEqual=$UPDATED_PRICE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByPriceIsLessThanOrEqualToSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where price is less than or equal to DEFAULT_PRICE
        defaultProductShouldBeFound("price.lessThanOrEqual=$DEFAULT_PRICE")

        // Get all the productList where price is less than or equal to SMALLER_PRICE
        defaultProductShouldNotBeFound("price.lessThanOrEqual=$SMALLER_PRICE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByPriceIsLessThanSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where price is less than DEFAULT_PRICE
        defaultProductShouldNotBeFound("price.lessThan=$DEFAULT_PRICE")

        // Get all the productList where price is less than UPDATED_PRICE
        defaultProductShouldBeFound("price.lessThan=$UPDATED_PRICE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByPriceIsGreaterThanSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where price is greater than DEFAULT_PRICE
        defaultProductShouldNotBeFound("price.greaterThan=$DEFAULT_PRICE")

        // Get all the productList where price is greater than SMALLER_PRICE
        defaultProductShouldBeFound("price.greaterThan=$SMALLER_PRICE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByRemainingCountIsEqualToSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where remainingCount equals to DEFAULT_REMAINING_COUNT
        defaultProductShouldBeFound("remainingCount.equals=$DEFAULT_REMAINING_COUNT")

        // Get all the productList where remainingCount equals to UPDATED_REMAINING_COUNT
        defaultProductShouldNotBeFound("remainingCount.equals=$UPDATED_REMAINING_COUNT")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByRemainingCountIsNotEqualToSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where remainingCount not equals to DEFAULT_REMAINING_COUNT
        defaultProductShouldNotBeFound("remainingCount.notEquals=$DEFAULT_REMAINING_COUNT")

        // Get all the productList where remainingCount not equals to UPDATED_REMAINING_COUNT
        defaultProductShouldBeFound("remainingCount.notEquals=$UPDATED_REMAINING_COUNT")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByRemainingCountIsInShouldWork() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where remainingCount in DEFAULT_REMAINING_COUNT or UPDATED_REMAINING_COUNT
        defaultProductShouldBeFound("remainingCount.in=$DEFAULT_REMAINING_COUNT,$UPDATED_REMAINING_COUNT")

        // Get all the productList where remainingCount equals to UPDATED_REMAINING_COUNT
        defaultProductShouldNotBeFound("remainingCount.in=$UPDATED_REMAINING_COUNT")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByRemainingCountIsNullOrNotNull() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where remainingCount is not null
        defaultProductShouldBeFound("remainingCount.specified=true")

        // Get all the productList where remainingCount is null
        defaultProductShouldNotBeFound("remainingCount.specified=false")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByRemainingCountIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where remainingCount is greater than or equal to DEFAULT_REMAINING_COUNT
        defaultProductShouldBeFound("remainingCount.greaterThanOrEqual=$DEFAULT_REMAINING_COUNT")

        // Get all the productList where remainingCount is greater than or equal to UPDATED_REMAINING_COUNT
        defaultProductShouldNotBeFound("remainingCount.greaterThanOrEqual=$UPDATED_REMAINING_COUNT")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByRemainingCountIsLessThanOrEqualToSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where remainingCount is less than or equal to DEFAULT_REMAINING_COUNT
        defaultProductShouldBeFound("remainingCount.lessThanOrEqual=$DEFAULT_REMAINING_COUNT")

        // Get all the productList where remainingCount is less than or equal to SMALLER_REMAINING_COUNT
        defaultProductShouldNotBeFound("remainingCount.lessThanOrEqual=$SMALLER_REMAINING_COUNT")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByRemainingCountIsLessThanSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where remainingCount is less than DEFAULT_REMAINING_COUNT
        defaultProductShouldNotBeFound("remainingCount.lessThan=$DEFAULT_REMAINING_COUNT")

        // Get all the productList where remainingCount is less than UPDATED_REMAINING_COUNT
        defaultProductShouldBeFound("remainingCount.lessThan=$UPDATED_REMAINING_COUNT")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByRemainingCountIsGreaterThanSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where remainingCount is greater than DEFAULT_REMAINING_COUNT
        defaultProductShouldNotBeFound("remainingCount.greaterThan=$DEFAULT_REMAINING_COUNT")

        // Get all the productList where remainingCount is greater than SMALLER_REMAINING_COUNT
        defaultProductShouldBeFound("remainingCount.greaterThan=$SMALLER_REMAINING_COUNT")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByStatusIsEqualToSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where status equals to DEFAULT_STATUS
        defaultProductShouldBeFound("status.equals=$DEFAULT_STATUS")

        // Get all the productList where status equals to UPDATED_STATUS
        defaultProductShouldNotBeFound("status.equals=$UPDATED_STATUS")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByStatusIsNotEqualToSomething() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where status not equals to DEFAULT_STATUS
        defaultProductShouldNotBeFound("status.notEquals=$DEFAULT_STATUS")

        // Get all the productList where status not equals to UPDATED_STATUS
        defaultProductShouldBeFound("status.notEquals=$UPDATED_STATUS")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByStatusIsInShouldWork() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultProductShouldBeFound("status.in=$DEFAULT_STATUS,$UPDATED_STATUS")

        // Get all the productList where status equals to UPDATED_STATUS
        defaultProductShouldNotBeFound("status.in=$UPDATED_STATUS")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllProductsByStatusIsNullOrNotNull() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        // Get all the productList where status is not null
        defaultProductShouldBeFound("status.specified=true")

        // Get all the productList where status is null
        defaultProductShouldNotBeFound("status.specified=false")
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */

    @Throws(Exception::class)
    private fun defaultProductShouldBeFound(filter: String) {
        restProductMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(product.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].remainingCount").value(hasItem(DEFAULT_REMAINING_COUNT)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))

        // Check, that the count call also returns 1
        restProductMockMvc.perform(get(ENTITY_API_URL + "/count?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"))
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    @Throws(Exception::class)
    private fun defaultProductShouldNotBeFound(filter: String) {
        restProductMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)

        // Check, that the count call also returns 0
        restProductMockMvc.perform(get(ENTITY_API_URL + "/count?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"))
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingProduct() {
        // Get the product
        restProductMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putNewProduct() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        val databaseSizeBeforeUpdate = productRepository.findAll().size

        // Update the product
        val updatedProduct = productRepository.findById(product.id).get()
        // Disconnect from session so that the updates on updatedProduct are not directly saved in db
        em.detach(updatedProduct)
        updatedProduct.name = UPDATED_NAME
        updatedProduct.description = UPDATED_DESCRIPTION
        updatedProduct.price = UPDATED_PRICE
        updatedProduct.remainingCount = UPDATED_REMAINING_COUNT
        updatedProduct.status = UPDATED_STATUS
        val productDTO = productMapper.toDto(updatedProduct)

        restProductMockMvc.perform(
            put(ENTITY_API_URL_ID, productDTO.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(productDTO))
        ).andExpect(status().isOk)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
        val testProduct = productList[productList.size - 1]
        assertThat(testProduct.name).isEqualTo(UPDATED_NAME)
        assertThat(testProduct.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testProduct.price?.stripTrailingZeros()).isEqualTo(UPDATED_PRICE.stripTrailingZeros())
        assertThat(testProduct.remainingCount).isEqualTo(UPDATED_REMAINING_COUNT)
        assertThat(testProduct.status).isEqualTo(UPDATED_STATUS)
    }

    @Test
    @Transactional
    fun putNonExistingProduct() {
        val databaseSizeBeforeUpdate = productRepository.findAll().size
        product.id = count.incrementAndGet()

        // Create the Product
        val productDTO = productMapper.toDto(product)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(
            put(ENTITY_API_URL_ID, productDTO.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(productDTO))
        )
            .andExpect(status().isBadRequest)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchProduct() {
        val databaseSizeBeforeUpdate = productRepository.findAll().size
        product.id = count.incrementAndGet()

        // Create the Product
        val productDTO = productMapper.toDto(product)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(productDTO))
        ).andExpect(status().isBadRequest)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamProduct() {
        val databaseSizeBeforeUpdate = productRepository.findAll().size
        product.id = count.incrementAndGet()

        // Create the Product
        val productDTO = productMapper.toDto(product)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(
            put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(productDTO))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateProductWithPatch() {
        productRepository.saveAndFlush(product)

        val databaseSizeBeforeUpdate = productRepository.findAll().size

// Update the product using partial update
        val partialUpdatedProduct = Product().apply {
            id = product.id

            name = UPDATED_NAME
            description = UPDATED_DESCRIPTION
            remainingCount = UPDATED_REMAINING_COUNT
        }

        restProductMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedProduct.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedProduct))
        )
            .andExpect(status().isOk)

// Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
        val testProduct = productList.last()
        assertThat(testProduct.name).isEqualTo(UPDATED_NAME)
        assertThat(testProduct.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testProduct.price?.stripTrailingZeros()).isEqualByComparingTo(DEFAULT_PRICE.stripTrailingZeros())
        assertThat(testProduct.remainingCount).isEqualTo(UPDATED_REMAINING_COUNT)
        assertThat(testProduct.status).isEqualTo(DEFAULT_STATUS)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateProductWithPatch() {
        productRepository.saveAndFlush(product)

        val databaseSizeBeforeUpdate = productRepository.findAll().size

// Update the product using partial update
        val partialUpdatedProduct = Product().apply {
            id = product.id

            name = UPDATED_NAME
            description = UPDATED_DESCRIPTION
            price = UPDATED_PRICE
            remainingCount = UPDATED_REMAINING_COUNT
            status = UPDATED_STATUS
        }

        restProductMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedProduct.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedProduct))
        )
            .andExpect(status().isOk)

// Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
        val testProduct = productList.last()
        assertThat(testProduct.name).isEqualTo(UPDATED_NAME)
        assertThat(testProduct.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testProduct.price?.stripTrailingZeros()).isEqualByComparingTo(UPDATED_PRICE.stripTrailingZeros())
        assertThat(testProduct.remainingCount).isEqualTo(UPDATED_REMAINING_COUNT)
        assertThat(testProduct.status).isEqualTo(UPDATED_STATUS)
    }

    @Throws(Exception::class)
    fun patchNonExistingProduct() {
        val databaseSizeBeforeUpdate = productRepository.findAll().size
        product.id = count.incrementAndGet()

        // Create the Product
        val productDTO = productMapper.toDto(product)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(
            patch(ENTITY_API_URL_ID, productDTO.id)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(productDTO))
        )
            .andExpect(status().isBadRequest)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchProduct() {
        val databaseSizeBeforeUpdate = productRepository.findAll().size
        product.id = count.incrementAndGet()

        // Create the Product
        val productDTO = productMapper.toDto(product)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(
            patch(ENTITY_API_URL_ID, count.incrementAndGet())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(productDTO))
        )
            .andExpect(status().isBadRequest)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamProduct() {
        val databaseSizeBeforeUpdate = productRepository.findAll().size
        product.id = count.incrementAndGet()

        // Create the Product
        val productDTO = productMapper.toDto(product)

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductMockMvc.perform(
            patch(ENTITY_API_URL)
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(productDTO))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Product in the database
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteProduct() {
        // Initialize the database
        productRepository.saveAndFlush(product)

        val databaseSizeBeforeDelete = productRepository.findAll().size

        // Delete the product
        restProductMockMvc.perform(
            delete(ENTITY_API_URL_ID, product.id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val productList = productRepository.findAll()
        assertThat(productList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"

        private const val DEFAULT_DESCRIPTION = "AAAAAAAAAA"
        private const val UPDATED_DESCRIPTION = "BBBBBBBBBB"

        private val DEFAULT_PRICE: BigDecimal = BigDecimal(0)
        private val UPDATED_PRICE: BigDecimal = BigDecimal(1)
        private val SMALLER_PRICE: BigDecimal = BigDecimal(0 - 1)

        private const val DEFAULT_REMAINING_COUNT: Int = 0
        private const val UPDATED_REMAINING_COUNT: Int = 1
        private const val SMALLER_REMAINING_COUNT: Int = 0 - 1

        private val DEFAULT_STATUS: SalesStatus = SalesStatus.AVAILABLE
        private val UPDATED_STATUS: SalesStatus = SalesStatus.SOLD_OUT

        private val ENTITY_API_URL: String = "/api/products"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + (2 * Integer.MAX_VALUE))

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Product {
            val product = Product(
                name = DEFAULT_NAME,

                description = DEFAULT_DESCRIPTION,

                price = DEFAULT_PRICE,

                remainingCount = DEFAULT_REMAINING_COUNT,

                status = DEFAULT_STATUS

            )

            return product
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Product {
            val product = Product(
                name = UPDATED_NAME,

                description = UPDATED_DESCRIPTION,

                price = UPDATED_PRICE,

                remainingCount = UPDATED_REMAINING_COUNT,

                status = UPDATED_STATUS

            )

            return product
        }
    }
}
