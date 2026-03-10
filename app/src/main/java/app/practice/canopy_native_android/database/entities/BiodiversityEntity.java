package app.practice.canopy_native_android.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "biodiversity_data",
        foreignKeys = @ForeignKey(
                entity = SurveyEntity.class,
                parentColumns = "survey_id",
                childColumns = "survey_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = "survey_id")
)
public class BiodiversityEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "survey_id")
    private String surveyId;

    @ColumnInfo(name = "wildlife_sighting")
    private Boolean wildlifeSighting;

    // JSON array string: ["species1", "species2"]
    @ColumnInfo(name = "wildlife_species")
    private String wildlifeSpecies;

    @ColumnInfo(name = "wildlife_evidence_type")
    private String wildlifeEvidenceType;

    @ColumnInfo(name = "sensitive_habitat_present")
    private Boolean sensitiveHabitatPresent;

    @ColumnInfo(name = "sensitive_habitat_type")
    private String sensitiveHabitatType;

    @ColumnInfo(name = "protected_species_flagged")
    private Boolean protectedSpeciesFlagged;

    @ColumnInfo(name = "protected_species_notes")
    private String protectedSpeciesNotes;

    public BiodiversityEntity(@NonNull String surveyId) {
        this.surveyId = surveyId;
    }

    // Getters and Setters
    @NonNull
    public String getSurveyId() { return surveyId; }
    public void setSurveyId(@NonNull String surveyId) { this.surveyId = surveyId; }

    public Boolean getWildlifeSighting() { return wildlifeSighting; }
    public void setWildlifeSighting(Boolean wildlifeSighting) { this.wildlifeSighting = wildlifeSighting; }

    public String getWildlifeSpecies() { return wildlifeSpecies; }
    public void setWildlifeSpecies(String wildlifeSpecies) { this.wildlifeSpecies = wildlifeSpecies; }

    public String getWildlifeEvidenceType() { return wildlifeEvidenceType; }
    public void setWildlifeEvidenceType(String wildlifeEvidenceType) { this.wildlifeEvidenceType = wildlifeEvidenceType; }

    public Boolean getSensitiveHabitatPresent() { return sensitiveHabitatPresent; }
    public void setSensitiveHabitatPresent(Boolean sensitiveHabitatPresent) { this.sensitiveHabitatPresent = sensitiveHabitatPresent; }

    public String getSensitiveHabitatType() { return sensitiveHabitatType; }
    public void setSensitiveHabitatType(String sensitiveHabitatType) { this.sensitiveHabitatType = sensitiveHabitatType; }

    public Boolean getProtectedSpeciesFlagged() { return protectedSpeciesFlagged; }
    public void setProtectedSpeciesFlagged(Boolean protectedSpeciesFlagged) { this.protectedSpeciesFlagged = protectedSpeciesFlagged; }

    public String getProtectedSpeciesNotes() { return protectedSpeciesNotes; }
    public void setProtectedSpeciesNotes(String protectedSpeciesNotes) { this.protectedSpeciesNotes = protectedSpeciesNotes; }
}