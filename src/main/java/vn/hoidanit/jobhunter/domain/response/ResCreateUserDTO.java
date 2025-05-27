package vn.hoidanit.jobhunter.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.util.constant.GenderEnum;

@Getter
@Setter
public class ResCreateUserDTO {
    private long id;
    private String name;
    private String email;
    private int age;
    private String address;
    private GenderEnum gender;
    private Instant createdAt;
    private CompanyUser company;

    @Getter
    @Setter
    public static class CompanyUser {
        private long id;
        private String name;
    }

}
