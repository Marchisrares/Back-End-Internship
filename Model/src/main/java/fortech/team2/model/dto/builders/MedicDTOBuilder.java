package fortech.team2.model.dto.builders;

import fortech.team2.model.Medic;
import fortech.team2.model.dto.MedicDTO;

import java.util.ArrayList;

public class MedicDTOBuilder {
    private MedicDTOBuilder() {
    }

    public static MedicDTO toMedicDTO(Medic medic) {
        return MedicDTO.builder()
                .id(medic.getId())
                .firstName(medic.getFirstName())
                .lastName(medic.getLastName())
                .education(medic.getEducation())
                .experience(medic.getExperience())
                .email(medic.getEmail())
                .phone(medic.getPhone())
                .specializations(medic.getSpecializations())
                .build();
    }


    public static Iterable<MedicDTO> toMedicDTOList(Iterable<Medic> medics) {
        ArrayList<MedicDTO> medicDTOS = new ArrayList<>();
        medics.forEach(medic -> medicDTOS.add(toMedicDTO(medic)));

        return medicDTOS;
    }
}