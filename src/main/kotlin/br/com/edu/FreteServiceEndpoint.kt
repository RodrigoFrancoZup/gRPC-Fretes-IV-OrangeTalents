package br.com.edu

import com.google.protobuf.Any
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FreteServiceEndpoint : FretesServiceGrpc.FretesServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun calculaFrete(request: FreteRequest?, responseObserver: StreamObserver<FreteResponse>?) {

        var frete = 0.0

        var cep = request?.cep
        if (cep == null || cep.isBlank()) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("Cep deve ser informado!")
                .asRuntimeException()
            responseObserver?.onError(e)
        }

        if (!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("Cep inválido!")
                .augmentDescription("Formato esperado deve ser 99999-999")
                .asRuntimeException()
            responseObserver?.onError(e)
        }

        try {
            frete = Random.nextDouble(from = 0.0, until = 1000.0) // Logica que poderia ser complexa
            /*
            Codigo para forçãr um erro:
            if(frete > 1){
                throw IllegalArgumentException("Forcei o erro, apenas para testar o tratamento!")
            }
             */
        } catch (e: Exception) {
            responseObserver?.onError(
                Status.INTERNAL
                    .withDescription(e.message)
                    .withCause(e) // Esse é anexado ao Status, mas não é enviado ao Client
                    .asRuntimeException()
            )
        }

        //Simulaçao de verificação de Segurança. Se temrinar com 333 ocorrerá um erro!
        if (cep.endsWith("333")) {
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Code.PERMISSION_DENIED.number)
                .setMessage("Erro de Segurança!")
                .addDetails(
                    Any.pack(
                        ErrorDetails.newBuilder()
                            .setCode(401)
                            .setMessage("Aqui eu explico o motivo")
                            .build()
                    )
                )
                .build()

            val e = StatusProto.toStatusRuntimeException(statusProto)
            responseObserver?.onError(e)
        }

        val response = FreteResponse.newBuilder()
            .setCep(cep)
            .setValor(frete)
            .build()

        //Log
        logger.info("O frete será: $frete")

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()

    }
}