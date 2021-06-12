package com.study.studyservice.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
public class StudyTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public static StudyTag createStudyTag(Tag tag){
        StudyTag studyTag = new StudyTag();
        studyTag.tag = tag;
        return studyTag;
    }

    public void setStudy(Study study){
        study.getStudyTags().add(this);
        this.study = study;
    }



}
