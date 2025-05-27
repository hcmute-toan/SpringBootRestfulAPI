package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CompanyService companyService;

    public UserService(UserRepository userRepository, CompanyService companyService) {
        this.companyService = companyService;
        this.userRepository = userRepository;
    }


    public User handleCreateUser(User user) {
        //check company
        if(user.getCompany() != null) {
            Optional<Company> companyOptional = this.companyService.findById(user.getCompany().getId());
            user.setCompany(companyOptional.isPresent()? companyOptional.get() : null);
        }
        return userRepository.save(user);
    }

    public ResCreateUserDTO handleConvertToCreateUserDTO(User user) {
        ResCreateUserDTO createUserDTO = new ResCreateUserDTO();
        ResCreateUserDTO.CompanyUser com = new ResCreateUserDTO.CompanyUser();
        createUserDTO.setId(user.getId());
        createUserDTO.setName(user.getName());
        createUserDTO.setEmail(user.getEmail());
        createUserDTO.setAge(user.getAge());
        createUserDTO.setAddress(user.getAddress());
        createUserDTO.setGender(user.getGender());
        createUserDTO.setCreatedAt(user.getCreatedAt());

        if(user.getCompany() != null) {
            com.setId(user.getCompany().getId());
            com.setName(user.getCompany().getName());
            createUserDTO.setCompany(com);
        }
        return createUserDTO;
    }


    public void handleDeleteUser(long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }


    public User handleGetUserById(long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        return null;
    }

    public ResUserDTO handleConvertToResUserDTO(User user) {
        ResUserDTO res = new ResUserDTO();
        ResUserDTO.CompanyUser com = new ResUserDTO.CompanyUser();
        if (user.getCompany() != null) {
            com.setId(user.getCompany().getId());
            com.setName(user.getCompany().getName());
            res.setCompany(com);
        }
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setUpdatedAt(user.getUpdatedAt());
        res.setCreatedAt(user.getCreatedAt());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        return res;
    }


    public ResultPaginationDTO handleGetAllUser(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO resultDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());

        resultDTO.setMeta(meta);
        //remove sentitive data
        List<ResUserDTO> listUser= pageUser.getContent()
            .stream().map(item -> new ResUserDTO(
                item.getId(),
                item.getName(),
                item.getEmail(),
                item.getGender(),
                item.getAddress(),
                item.getAge(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                new ResUserDTO.CompanyUser(
                    item.getCompany() != null ? item.getCompany().getId() : 0,
                    item.getCompany() != null ? item.getCompany().getName() : ""
                )
            ))
            .collect(Collectors.toList());
        resultDTO.setResult(listUser);
        return resultDTO;
    }


    public User handleUpdateUser(User reqUser) {
        User currentUser = this.handleGetUserById(reqUser.getId());
        if (currentUser != null) {
            currentUser.setAddress(reqUser.getAddress());
            currentUser.setGender(reqUser.getGender());
            currentUser.setAge(reqUser.getAge());
            currentUser.setName(reqUser.getName());

            //check company
            if(reqUser.getCompany() != null) {
                Optional<Company> companyOptional = this.companyService.findById(reqUser.getCompany().getId());
                currentUser.setCompany(companyOptional.isPresent()? companyOptional.get() : null);
            }
            // update
            currentUser = this.userRepository.save(currentUser);
        }
        return currentUser;
    }

    public ResUpdateUserDTO handleConvertToUpdateUserDTO(User user) {
        ResUpdateUserDTO res = new ResUpdateUserDTO();
        ResUpdateUserDTO.CompanyUser com = new ResUpdateUserDTO.CompanyUser();
        if (user.getCompany() != null) {
            com.setId(user.getCompany().getId());
            com.setName(user.getCompany().getName());
            res.setCompany(com);
        }
        res.setId(user.getId());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setUpdatedAt(user.getUpdatedAt());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        return res;
    }

    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    public boolean isEmailExist(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public void handleSetRefreshToken(String refreshToken, String email) {
        User currentUser = this.handleGetUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(refreshToken);
            this.userRepository.save(currentUser);
        }
    }
    public User handleGetUserByRefreshTokenAndEmail(String token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(token, email);
    }
}
