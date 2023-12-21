package br.com.cotiinformatica.services;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.cotiinformatica.components.JwtTokenComponent;
import br.com.cotiinformatica.dtos.AutenticarUsuarioRequestDto;
import br.com.cotiinformatica.dtos.AutenticarUsuarioResponseDto;
import br.com.cotiinformatica.dtos.CriarUsuarioRequestDto;
import br.com.cotiinformatica.dtos.CriarUsuarioResponseDto;
import br.com.cotiinformatica.entities.Usuario;
import br.com.cotiinformatica.helpers.SHA1CryptoHelper;
import br.com.cotiinformatica.repositories.UsuarioRepository;

@Service
public class UsuarioService {

	@Autowired // autoinicialização
	UsuarioRepository usuarioRepository;

	@Autowired // autoinicialização
	JwtTokenComponent jwtTokenComponent;

	/*
	 * Método de serviço para criar um usuário no sistema
	 */
	public CriarUsuarioResponseDto criarUsuario(CriarUsuarioRequestDto request) {

		// verificar se o email informado já está cadastrado no banco de dados
		if (usuarioRepository.find(request.getEmail()) != null) {
			throw new IllegalArgumentException("O email informado já está cadastrado para outro usuário.");
		}

		// capturar os dados do usuário
		Usuario usuario = new Usuario();
		usuario.setIdUsuario(UUID.randomUUID());
		usuario.setNome(request.getNome());
		usuario.setEmail(request.getEmail());
		usuario.setSenha(SHA1CryptoHelper.encrypt(request.getSenha()));

		// gravar no banco de dados
		usuarioRepository.save(usuario);

		CriarUsuarioResponseDto response = new CriarUsuarioResponseDto();
		response.setIdUsuario(usuario.getIdUsuario());
		response.setNome(usuario.getNome());
		response.setEmail(usuario.getEmail());
		response.setDataHoraCadastro(new Date());

		return response;
	}

	/*
	 * Método de serviço para autenticar um usuário no sistema
	 */
	public AutenticarUsuarioResponseDto autenticarUsuario(AutenticarUsuarioRequestDto request) {

		// buscar o usuário no banco de dados através do email e da senha
		Usuario usuario = usuarioRepository.find(request.getEmail(), SHA1CryptoHelper.encrypt(request.getSenha()));

		// verificar se o usuário não foi encontrado
		if (usuario == null)
			throw new IllegalArgumentException("Acesso negado. Usuário não encontrado.");

		// gerando os dados do usuário autenticado
		AutenticarUsuarioResponseDto response = new AutenticarUsuarioResponseDto();
		response.setIdUsuario(usuario.getIdUsuario());
		response.setNome(usuario.getNome());
		response.setEmail(usuario.getEmail());
		response.setDataHoraAcesso(new Date());
		response.setToken(jwtTokenComponent.generateToken(usuario));
		response.setDataHoraExpiracao(jwtTokenComponent.getExpirationDate());

		return response;
	}
}
