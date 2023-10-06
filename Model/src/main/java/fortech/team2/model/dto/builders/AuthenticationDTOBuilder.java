package fortech.team2.model.dto.builders;

import fortech.team2.model.Medic;
import fortech.team2.model.User;
import fortech.team2.model.dto.request.SignupMedicRequest;
import fortech.team2.model.dto.request.SignupRequest;
import fortech.team2.model.enums.UserRole;

public class AuthenticationDTOBuilder {

    private AuthenticationDTOBuilder() {
    }

    public static User fromSignupRequest(SignupRequest signupRequest) {
        return User.builder()
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLastName())
                .address(signupRequest.getAddress())
                .phone(signupRequest.getPhone())
                .email(signupRequest.getEmail())
                .role(signupRequest.getIsAdmin() != null && signupRequest.getIsAdmin()
                        ? UserRole.ROLE_ADMIN
                        : UserRole.ROLE_CUSTOMER)
                .build();
    }

    public static Medic fromSignupRequest(SignupMedicRequest signupMedicRequest) {
        return Medic.builder()
                .firstName(signupMedicRequest.getFirstName())
                .lastName(signupMedicRequest.getLastName())
                .address(signupMedicRequest.getAddress())
                .phone(signupMedicRequest.getPhone())
                .email(signupMedicRequest.getEmail())
                .education(signupMedicRequest.getEducation())
                .specializations(signupMedicRequest.getSpecializations())
                .role(UserRole.ROLE_MEDIC)
                .experience(signupMedicRequest.getExperience())
                .build();
    }

    public static SignupRequest toRegisterAccountDTO(User user) {
        return SignupRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .address(user.getAddress())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .email(user.getEmail())
                .build();
    }
}
