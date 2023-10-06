package fortech.team2.services.impl;

import fortech.team2.model.UserPreference;
import fortech.team2.model.dto.UserPreferenceDTO;
import fortech.team2.model.dto.builders.UserPreferenceDTOBuilder;
import fortech.team2.persistence.UserPreferenceRepository;
import fortech.team2.persistence.exceptions.RepositoryException;
import fortech.team2.services.UserPreferenceService;
import fortech.team2.services.UserService;
import fortech.team2.validation.UserPreferenceValidator;
import fortech.team2.validation.utils.ValidatorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPreferenceServiceImpl implements UserPreferenceService {

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserPreferenceValidator userPreferenceValidator;

    @Override
    public UserPreference addUserPreference(UserPreferenceDTO preferenceDTO) throws ValidatorException, RepositoryException {
        //validate
        userPreferenceValidator.validate(preferenceDTO);

        // verify if it doesnt already exists
        if (checkIfUserPreferenceAlreadyExists(preferenceDTO)) {
            // if already exists
            throw new RepositoryException("User preference already exists!");
        }

        //builder
        UserPreference preference = UserPreferenceDTOBuilder.fromUserPreferenceDTO(preferenceDTO);
        preference.setCustomer(userService.getUserById(preferenceDTO.getCustomerId()));

        //save
        return userPreferenceRepository.save(preference);
    }

    @Override
    public Iterable<UserPreference> getUserPreferencesByUserId(Integer userId) {
        return userPreferenceRepository.getUserPreferencesByCustomer_Id(userId);
    }

    @Override
    public Boolean checkIfUserPreferenceAlreadyExists(UserPreferenceDTO preferenceDTO) throws RepositoryException {
        //validate
        try {
            userPreferenceValidator.validate(preferenceDTO);
        } catch (ValidatorException e) {
            return false;
        }

        //builder
        UserPreference preference = UserPreferenceDTOBuilder.fromUserPreferenceDTO(preferenceDTO);
        preference.setCustomer(userService.getUserById(preferenceDTO.getCustomerId()));

        //check repo
        preference = userPreferenceRepository.getUserPreferenceByNameAndSexAndTypeAndBreedAndColourAndAgeAndFirstNameAndLastNameAndEmailAndPhoneAndAddress(
                preference.getName(), preference.getSex(), preference.getType(), preference.getBreed(),
                preference.getColour(), preference.getAge(), preference.getFirstName(), preference.getLastName(),
                preference.getEmail(), preference.getPhone(), preference.getAddress()
        );

        return preference != null;
    }

    @Override
    public UserPreference getUserPreferenceById(Integer id) throws RepositoryException {
        return userPreferenceRepository.findById(id).orElseThrow(() -> new RepositoryException("Preference not found"));
    }

    @Override
    public UserPreference updateUserPreference(UserPreferenceDTO userPreferenceDTO) throws ValidatorException, RepositoryException {
        userPreferenceValidator.validate(userPreferenceDTO);
        UserPreference preference = UserPreferenceDTOBuilder.fromUserPreferenceDTO(userPreferenceDTO);
        preference.setCustomer(userService.getUserById(userPreferenceDTO.getCustomerId()));
        return userPreferenceRepository.save(preference);
    }

    @Override
    public void deleteUserPreference(Integer id) throws RepositoryException {
        userPreferenceRepository.findById(id).orElseThrow(() -> new RepositoryException("Preference not found"));
        userPreferenceRepository.deleteById(id);
    }
}
