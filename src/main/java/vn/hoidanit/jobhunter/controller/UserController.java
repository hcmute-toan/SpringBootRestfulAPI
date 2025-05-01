package vn.hoidanit.jobhunter.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.annotation.APIMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    // @GetMapping("/users/create")
    @PostMapping("/users")
    @APIMessage("Create new user")
    public ResponseEntity<ResCreateUserDTO> CreateNewUser(@Valid @RequestBody User postmanUser)
            throws IdInvalidException {
        boolean isEmailExist = this.userService.isEmailExist(postmanUser.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException("Email " + postmanUser.getEmail() + " is exist, please try again");
        }
        String hashPassword = passwordEncoder.encode(postmanUser.getPassword());
        postmanUser.setPassword(hashPassword);
        User userCreated = userService.handleCreateUser(postmanUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.userService.handleConvertToCreateUserDTO(userCreated));
    }

    @DeleteMapping("/users/{id}")
    @APIMessage("Remove user")
    public ResponseEntity<String> DeleteUser(@PathVariable("id") long id) throws IdInvalidException {
        User currentUser = this.userService.handleGetUserById(id);
        if (currentUser == null) {
            throw new IdInvalidException("The User's id = " + id + " not existing");
        }
        this.userService.handleDeleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body("Delete user with id: " + id + " success!");
    }

    @GetMapping("/users/{id}")
    @APIMessage("fetch user by id")
    public ResponseEntity<ResUserDTO> GetUser(@PathVariable("id") long id) throws IdInvalidException {
        User currentUser = this.userService.handleGetUserById(id);
        if (currentUser == null) {
            throw new IdInvalidException("The User's id = " + id + " not existing");
        }
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleConvertToResUserDTO(currentUser));
    }

    @GetMapping("/users")
    @APIMessage("fetch all users")
    public ResponseEntity<ResultPaginationDTO> GetAllUser(
            @Filter Specification<User> spec,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleGetAllUser(spec, pageable));
    }

    @PutMapping("/users")
    @APIMessage("Update user")
    public ResponseEntity<ResUpdateUserDTO> UpdateUser(@RequestBody User reqUser) throws IdInvalidException {
        User userUpdated = this.userService.handleUpdateUser(reqUser);
        if (reqUser == null) {
            throw new IdInvalidException("The User's id = " + reqUser.getId() + " not existing");
        }
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleConvertToUpdateUserDTO(userUpdated));
    }
}
