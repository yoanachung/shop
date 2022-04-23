package com.yon.shop.web.rest

import com.fasterxml.jackson.annotation.JsonProperty
import com.yon.shop.security.jwt.JWTFilter
import com.yon.shop.security.jwt.TokenProvider
import com.yon.shop.web.rest.vm.LoginVM
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import javax.validation.Valid

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
class UserJWTController(
    private val tokenProvider: TokenProvider,
    private val authenticationManager: ReactiveAuthenticationManager
) {
    @PostMapping("/authenticate")
    fun authorize(@Valid @RequestBody loginVM: Mono<LoginVM>): Mono<ResponseEntity<JWTToken>> =
        loginVM.flatMap { login ->
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(login.username, login.password))
                .map { tokenProvider.createToken(it, true == login.isRememberMe) }
        }.map {
            jwt ->
            val httpHeaders = HttpHeaders()
            httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer $jwt")
            ResponseEntity(JWTToken(jwt), httpHeaders, HttpStatus.OK)
        }

    /**
     * Object to return as body in JWT Authentication.
     */
    class JWTToken(@get:JsonProperty("id_token") var idToken: String?)
}
