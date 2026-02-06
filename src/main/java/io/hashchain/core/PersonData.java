package io.hashchain.core;

import java.time.LocalDate;

public class PersonData {
    private String firstName;
    private String lastName;
    private String patronymic;
    private LocalDate birthDate;
    private FingerprintDot fingerprintDot;

    public PersonData(String firstName, String lastName, String patronymic, LocalDate birthDate, FingerprintDot fingerprintDot) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.patronymic = patronymic;
        this.birthDate = birthDate;
        this.fingerprintDot = fingerprintDot;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPatronymic() { return patronymic; }
    public LocalDate getBirthDate() { return birthDate; }
    public FingerprintDot getFingerprintDot() { return fingerprintDot; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public void setFingerprintDot(FingerprintDot fingerprintDot) { this.fingerprintDot = fingerprintDot; }

    @Override
    public String toString() {
        return firstName + " " + patronymic + " " + lastName + " " + birthDate + " " + fingerprintDot;
    }
}
