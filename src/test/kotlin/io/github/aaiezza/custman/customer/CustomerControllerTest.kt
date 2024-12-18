package io.github.aaiezza.custman.customer

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.aaiezza.custman.customer.data.*
import io.github.aaiezza.custman.customer.models.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.*

@RestClientTest
class CustomerControllerTest {

    // Mock all required executors
    private val createCustomerStatement: CreateCustomerStatement = mockk()
    private val getCustomerByIdStatement: GetCustomerByIdStatement = mockk()

    // Instantiate the controller with mocked executors
    private val customerController = CustomerController(
        createCustomerStatement,
    )

    @Test
    fun `createCustomer should return 201 CREATED with customer details`() {
        // Arrange
        val createRequest = CreateCustomerRequest.sample
        val createdCustomer = Customer.sample

        every { createCustomerStatement.execute(createRequest) } returns createdCustomer

        // Act
        val response: ResponseEntity<*> = customerController.createCustomer(createRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isEqualTo(createdCustomer)

        // Verify
        verify(exactly = 1) { createCustomerStatement.execute(createRequest) }
    }
}
