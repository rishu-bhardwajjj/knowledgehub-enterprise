
package com.knowledgehub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDocumentRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        @Size(max = 2000)
        String description,

        @NotBlank
        @Size(max = 100)
        String category,

        @NotBlank
        @Size(max = 100)
        String uploadedBy

) {
}
