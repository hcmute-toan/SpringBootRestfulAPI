package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.CompanyRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    public Company handleSaveCompany(Company company) {
        return this.companyRepository.save(company);
    }

    // get all companies
    public ResultPaginationDTO handleFetchAllCompanies(Specification<Company> spec, Pageable pageable) {
        Page<Company> pageCompany = this.companyRepository.findAll(spec, pageable);

        ResultPaginationDTO resultDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageCompany.getTotalPages());
        meta.setTotal(pageCompany.getTotalElements());

        resultDTO.setMeta(meta);
        resultDTO.setResult(pageCompany.getContent());
        return resultDTO;
    }

    // public Company handleFetchCompanyById(long id) {
    //     return this.companyRepository.findById(id);
    // }

    public void handleDeleteCompanyById(long id) {
        Optional<Company> comOptional = this.companyRepository.findById(id);
        if (comOptional.isPresent()) {
            Company com = comOptional.get();
            // fetch all user belong to this company
            List<User> users = this.userRepository.findByCompany(com);
            this.userRepository.deleteAll(users);
        }
        this.companyRepository.deleteById(id);
    }

    public Company handleUpdateCompany(Company company) {
        Optional<Company> companyOptional = this.companyRepository.findById(company.getId());
        if (companyOptional.isPresent()) {
            Company currentCompany = companyOptional.get();
            currentCompany.setName(company.getName());
            currentCompany.setAddress(company.getAddress());
            currentCompany.setDescription(company.getDescription());
            currentCompany.setLogo(company.getLogo());
            this.companyRepository.save(currentCompany);
            return this.companyRepository.save(currentCompany);
        }
        return null;
    }
    public Optional<Company> findById(long id) {
        return this.companyRepository.findById(id);
    }
}
