package com.communityhelp.app.review.mapper;

import com.communityhelp.app.review.dto.ReviewResponseDto;
import com.communityhelp.app.review.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct crea una bean de Spring.
 * IGNORE - Se ignoran los campos que no se puedan mapear.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", source = "author.name")
    @Mapping(target = "targetId", source = "target.id")
    @Mapping(target = "targetName", source = "target.name")
    @Mapping(target = "donationId", source = "donation.id")
    @Mapping(target = "helpRequestId", source = "helpRequest.id")
    ReviewResponseDto toDto(Review review);

}
