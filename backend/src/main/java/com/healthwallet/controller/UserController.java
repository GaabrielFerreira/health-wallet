package com.healthwallet.controller;

import com.healthwallet.dto.RegisterRequest;
import com.healthwallet.dto.RegisterResponse;
import com.healthwallet.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Operações de cadastro e gerenciamento de usuários")
public class UserController {

    private final AuthService authService;

    @Operation(summary = "Registrar novo usuário", description = "Cria uma nova conta de usuário no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso",
            content = @Content(schema = @Schema(implementation = RegisterResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou campos obrigatórios ausentes",
            content = @Content(schema = @Schema(implementation = Object.class))),
        @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
            content = @Content(schema = @Schema(implementation = Object.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
