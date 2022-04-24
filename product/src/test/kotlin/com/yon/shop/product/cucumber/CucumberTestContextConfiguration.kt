package com.yon.shop.product.cucumber

import com.yon.shop.product.IntegrationTest
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration

@CucumberContextConfiguration
@IntegrationTest
@WebAppConfiguration
class CucumberTestContextConfiguration
