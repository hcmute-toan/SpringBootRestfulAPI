package vn.hoidanit.jobhunter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.request.ReqLoginDTO;
import vn.hoidanit.jobhunter.domain.response.ResLoginDTO;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.annotation.APIMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

        private final AuthenticationManagerBuilder authenticationManagerBuilder;
        private final SecurityUtil securityUtil;
        private final UserService userService;

        @Value("${hoidanit.jwt.refresh-token-validity-in-seconds}")
        private long refreshTokenExpiration;

        public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,
                        SecurityUtil securityUtil, UserService userService) {
                this.authenticationManagerBuilder = authenticationManagerBuilder;
                this.securityUtil = securityUtil;
                this.userService = userService;
        }

        @PostMapping("/auth/login")
        public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDTO) {
                // nạp input bao gồm username/password vào Security
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                loginDTO.getUsername(), loginDTO.getPassword());

                // xác thực người dùng cần viết hàm loadUserByUsername
                Authentication authentication = authenticationManagerBuilder.getObject()
                                .authenticate(authenticationToken);

                // set thong tin nguoi dung dang nhap vao context (co the su dung sau nay)
                SecurityContextHolder.getContext().setAuthentication(authentication);

                ResLoginDTO restLoginDTO = new ResLoginDTO();
                User currentUserDB = this.userService.handleGetUserByUsername(loginDTO.getUsername());
                if (currentUserDB != null) {
                        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                                        currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName());
                        restLoginDTO.setUser(userLogin);

                }
                String accessToken = this.securityUtil.createAccessToken(authentication.getName(), restLoginDTO.getUser());
                restLoginDTO.setAccessToken(accessToken);

                // create refresh token
                String refresh_token = this.securityUtil.createRefreshToken(loginDTO.getUsername(), restLoginDTO);
                this.userService.handleSetRefreshToken(refresh_token, loginDTO.getUsername());

                // set cookies
                ResponseCookie resCookies = ResponseCookie
                                .from("refresh_token", refresh_token)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(refreshTokenExpiration)
                                // .domain("example.com")
                                .build();

                return ResponseEntity
                                .ok()
                                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                                .body(restLoginDTO);
        }

        @GetMapping("/auth/account")
        @APIMessage("fetch account")
        public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
                String email = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";

                User currentUserDB = this.userService.handleGetUserByUsername(email);
                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
                ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
                if (currentUserDB != null) {
                        userLogin.setId(currentUserDB.getId());
                        userLogin.setEmail(currentUserDB.getEmail());
                        userLogin.setName(currentUserDB.getName());
                        userGetAccount.setUser(userLogin);
                }

                return ResponseEntity.ok().body(userGetAccount);
        }

        @GetMapping("/auth/refresh")
        @APIMessage("Get user by refresh token")
        public ResponseEntity<ResLoginDTO> getRefreshToken(@CookieValue(name = "refresh_token",defaultValue = "abc") String refreshToken) throws IdInvalidException {
                if(refreshToken.equals("abc")){
                        throw new IdInvalidException("refresh token is invalid");
                }
                // check valid
                Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
                String email = decodedToken.getSubject();
                //check user by token + email
                User currentUser = this.userService.handleGetUserByRefreshTokenAndEmail(refreshToken, email);
                if (currentUser == null) {
                        throw new IdInvalidException("refresh token is invalid");
                }
                //issue new token /set refresh token as cookie

                ResLoginDTO restLoginDTO = new ResLoginDTO();
                User currentUserDB = this.userService.handleGetUserByUsername(email);
                if (currentUserDB != null) {
                        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                                        currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName());
                        restLoginDTO.setUser(userLogin);

                }
                String accessToken = this.securityUtil.createAccessToken(email, restLoginDTO.getUser());
                restLoginDTO.setAccessToken(accessToken);

                // create refresh token
                String new_refresh_token = this.securityUtil.createRefreshToken(email, restLoginDTO);
                this.userService.handleSetRefreshToken(new_refresh_token, email);

                // set cookies
                ResponseCookie resCookies = ResponseCookie
                                .from("refresh_token", new_refresh_token)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(refreshTokenExpiration)
                                // .domain("example.com")
                                .build();

                return ResponseEntity
                                .ok()
                                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                                .body(restLoginDTO);
        }
        @PostMapping("/auth/logout")
        @APIMessage("Logout User")
        public ResponseEntity<Void> logout() throws IdInvalidException {
               String email = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                //check user by token + email
                User currentUser = this.userService.handleGetUserByUsername(email);
                if(email.equals("")){
                        throw new IdInvalidException("Access token is invalid");
                }
                //update refresh token = null
                this.userService.handleSetRefreshToken(null, email);
                //remove refresh token cookie
                ResponseCookie deleteSpringCookie = ResponseCookie
                .from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                                .build();
        }
}
