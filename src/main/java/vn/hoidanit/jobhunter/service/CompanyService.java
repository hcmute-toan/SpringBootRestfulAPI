package vn.hoidanit.jobhunter.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.CompanyRepository;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
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
        meta.setTotalPages(pageCompany.getTotalPages());
        meta.setTotalElements(pageCompany.getTotalElements());

        resultDTO.setMeta(meta);
        resultDTO.setResult(pageCompany.getContent());
        return resultDTO;
    }

    public Company handleFetchCompanyById(long id) {
        return this.companyRepository.findById(id);
    }

    public void handleDeleteCompanyById(long id) {
        this.companyRepository.deleteById(id);
    }

    public Company handleUpdateCompany(Company company) {
        Company currentCompany = this.companyRepository.findById(company.getId());
        if (currentCompany != null) {
            currentCompany.setName(company.getName());
            currentCompany.setAddress(company.getAddress());
            currentCompany.setDescription(company.getDescription());
            currentCompany.setLogo(company.getLogo());
            this.companyRepository.save(currentCompany);
        }
        return currentCompany;
    }
}
