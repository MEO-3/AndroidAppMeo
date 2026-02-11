package org.thingai.android.module.meo.cloud

import org.thingai.meo.common.dto.product.RequestProductUpsert
import org.thingai.meo.common.dto.product.ResponseProductRead
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiProduct {
    @POST("/api/v1/products/{orgCode}/products")
    suspend fun createProduct(@Body requestBody: RequestProductUpsert): Response<ResponseProductRead>
}