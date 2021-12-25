package com.study.service;

import com.study.client.AreaResponse;
import com.study.client.AreaServiceClient;
import com.study.client.UserResponse;
import com.study.client.UserServiceClient;
import com.study.domain.*;
import com.study.dto.chatroom.ChatRoomCreateRequest;
import com.study.dto.chatroom.ChatRoomResponse;
import com.study.dto.chatroom.ChatRoomUpdateRequest;
import com.study.dto.study.*;
import com.study.dto.studyuser.StudyUserResponse;
import com.study.kakfa.StudyApplyFailMessage;
import com.study.kakfa.StudyApplySuccessMessage;
import com.study.kakfa.StudyCreateMessage;
import com.study.kakfa.StudyDeleteMessage;
import com.study.kakfa.sender.StudyApplyFailMessageSender;
import com.study.kakfa.sender.StudyApplySuccessMessageSender;
import com.study.kakfa.sender.StudyCreateMessageSender;
import com.study.kakfa.sender.StudyDeleteMessageSender;
import com.study.repository.CategoryRepository;
import com.study.repository.StudyQueryRepository;
import com.study.repository.StudyRepository;
import com.study.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyServiceImpl implements StudyService {

    private final StudyRepository studyRepository;
    private final StudyQueryRepository studyQueryRepository;
    private final CategoryRepository categoryRepository;
    private final AreaServiceClient areaServiceClient;
    private final UserServiceClient userServiceClient;
    private final ImageUtil imageUtil;

    private final StudyCreateMessageSender studyCreateMessageSender;
    private final StudyDeleteMessageSender studyDeleteMessageSender;
    private final StudyApplyFailMessageSender studyApplyFailMessageSender;
    private final StudyApplySuccessMessageSender studyApplySuccessMessageSender;

    @Override
    @Transactional
    public StudyResponse create(Long userId, MultipartFile file, StudyCreateRequest request) {
        Category findCategory = categoryRepository.findWithParentById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException());

        AreaResponse area = new AreaResponse();
        if (request.getOffline() == true) {
            area = areaServiceClient.findByCode(request.getAreaCode());
        }

        Image image = imageUtil.uploadImage(file, null);

        Study study = Study.createStudy(request.getName(), request.getContent(), request.getNumberOfPeople(),
                request.getOnline(), request.getOffline(), findCategory);
        study.addStudyUser(userId, StudyRole.ADMIN);
        study.changeImage(image);
        study.addTags(request.getTags());
        study.changeArea(area.getId());
        studyRepository.save(study);

        studyCreateMessageSender.send(StudyCreateMessage.from(study.getId(), study.getName(), request.getTags()));
        return StudyResponse.from(study, area);
    }

    @Override
    @Transactional
    public StudyResponse updateImage(Long userId, Long studyId, MultipartFile file) {
        Study findStudy = studyRepository.findWithStudyUserById(studyId)
                .orElseThrow(() -> new RuntimeException());
        findStudy.isStudyAdmin(userId);

        Image image = imageUtil.uploadImage(file, findStudy.getImage());
        findStudy.changeImage(image);
        return StudyResponse.from(findStudy);
    }

    @Override
    @Transactional
    public StudyResponse updateArea(Long userId, Long studyId, StudyUpdateAreaRequest request) {
        Study findStudy = studyRepository.findWithStudyUserById(studyId)
                .orElseThrow(() -> new RuntimeException());
        findStudy.isStudyAdmin(userId);

        AreaResponse area = areaServiceClient.findByCode(request.getAreaCode());
        findStudy.changeArea(area.getId());
        return StudyResponse.from(findStudy, area);
    }

    @Override
    @Transactional
    public StudyResponse update(Long userId, Long studyId, StudyUpdateRequest request) {
        Study findStudy = studyRepository.findWithStudyUserById(studyId)
                .orElseThrow(() -> new RuntimeException());
        findStudy.isStudyAdmin(userId);

        Category findCategory = categoryRepository.findWithParentById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException());

        findStudy.change(request.getName(), request.getContent(), request.getNumberOfPeople(),
                request.getOnline(), request.getOffline(), request.getOpen(), findCategory);

        return StudyResponse.from(findStudy);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long studyId) {
        Study findStudy = studyRepository.findWithStudyUserById(studyId)
                .orElseThrow(() -> new RuntimeException());
        findStudy.isStudyAdmin(userId);

        studyRepository.delete(findStudy);
        studyDeleteMessageSender.send(StudyDeleteMessage.from(findStudy.getId()));
    }

    @Override
    public StudyResponse findById(Long studyId) {
        Study findStudy = studyQueryRepository.findWithCategoryAndTagById(studyId);

        AreaResponse areaResponse = null;
        if (findStudy.getAreaId() != null && findStudy.isOffline()) {
            areaResponse = areaServiceClient.findById(findStudy.getAreaId());
        }

        return StudyResponse.from(findStudy, areaResponse);
    }

    @Override
    public Slice<StudyResponse> search(Long userId, Pageable pageable, StudySearchRequest request) {

        List<Long> areaIds = null;
        if (request.getOffline() == true && userId != null) {
            UserResponse user = userServiceClient.findById(userId);
            if (user.getAreaId() != null) {
                List<AreaResponse> areas = areaServiceClient.findAroundById(user.getAreaId(), user.getDistance());
                areaIds = areas.stream().map(area -> area.getId()).collect(Collectors.toList());
            }
        }
        Page<Study> findStudies = studyQueryRepository.findBySearchCondition(request, areaIds, pageable);
        return findStudies.map(study -> StudyResponse.fromWithTag(study));
    }

    @Override
    @Transactional
    public void createWaitUser(Long userId, Long studyId) {
        Study findStudy = studyRepository.findWithWaitUserById(studyId)
                .orElseThrow(() -> new RuntimeException());
        findStudy.addWaitUser(userId);
    }

    @Override
    @Transactional
    public void deleteWaitUser(Long userId, Long studyId) {
        Study findStudy = studyRepository.findWithWaitUserById(studyId)
                .orElseThrow(() -> new RuntimeException());
        findStudy.deleteWaitUser(userId);
    }

    @Override
    @Transactional
    public void deleteWaitUser(Long userId, Long studyId, Long waitUserId) {
        Study findStudy = studyRepository.findWithWaitUserById(studyId)
                .orElseThrow(() -> new RuntimeException());
        findStudy.isStudyAdmin(userId);
        findStudy.failWaitUser(waitUserId);

        studyApplyFailMessageSender.send(StudyApplyFailMessage.from(waitUserId, findStudy.getId(), findStudy.getName()));
    }

    @Override
    public List<UserResponse> findWaitUsersById(Long studyId) {
        Study findStudy = studyRepository.findWithWaitUserById(studyId)
                .orElseThrow(() -> new RuntimeException());

        List<Long> userIds = findStudy.getWaitUsersId();
        return userServiceClient.findByIdIn(userIds);
    }

    @Override
    @Transactional
    public void createStudyUser(Long userId, Long studyId, Long studyUserId) {
        Study findStudy = studyRepository.findWithWaitUserById(studyId)
                .orElseThrow(() -> new RuntimeException());
        findStudy.isStudyAdmin(userId);

        findStudy.successWaitUser(studyUserId);
        findStudy.addStudyUser(studyUserId, StudyRole.USER);
        studyApplySuccessMessageSender.send(StudyApplySuccessMessage.from(studyUserId, studyId, findStudy.getName()));
    }

    @Override
    @Transactional
    public void deleteStudyUser(Long userId, Long studyId, Long studyUserId) {
        Study findStudy = studyRepository.findWithStudyUserById(studyId)
                .orElseThrow(() -> new RuntimeException());
        findStudy.isStudyAdmin(userId);

        findStudy.deleteStudyUser(studyUserId);
    }

    @Override
    @Transactional
    public void deleteStudyUser(Long userId, Long studyId) {
        Study findStudy = studyRepository.findWithStudyUserById(studyId)
                .orElseThrow(() -> new RuntimeException());

        findStudy.deleteStudyUser(userId);
    }

    @Override
    public List<StudyUserResponse> findStudyUsersById(Long studyId) {
        Study findStudy = studyRepository.findWithStudyUserById(studyId)
                .orElseThrow(() -> new RuntimeException());
        List<Long> userIds = findStudy.getStudyUsersId();

        List<UserResponse> users = userServiceClient.findByIdIn(userIds);

        return findStudy.getStudyUsers().stream()
                .map(studyUser -> StudyUserResponse.from(studyUser, users))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createChatRoom(Long userId, Long studyId, ChatRoomCreateRequest request) {
        Study findStudy = studyRepository.findWithChatRoomById(studyId)
                .orElseThrow(() -> new RuntimeException());
        findStudy.isStudyAdmin(userId);

        findStudy.addChatRoom(request.getName());
    }

    @Override
    @Transactional
    public void updateChatRoom(Long userId, Long studyId, Long chatRoomId, ChatRoomUpdateRequest request) {
        Study findStudy = studyRepository.findWithChatRoomById(studyId)
                .orElseThrow(() -> new RuntimeException());
        findStudy.isStudyAdmin(userId);

        findStudy.updateChatRoom(chatRoomId, request.getName());
    }

    @Override
    @Transactional
    public void deleteChatRoom(Long userId, Long studyId, Long chatRoomId) {
        Study findStudy = studyRepository.findWithChatRoomById(studyId)
                .orElseThrow(() -> new RuntimeException());
        findStudy.isStudyAdmin(userId);

        findStudy.deleteChatRoom(chatRoomId);
    }

    @Override
    public List<ChatRoomResponse> findChatRoomsById(Long studyId) {
        Study findStudy = studyRepository.findWithChatRoomById(studyId)
                .orElseThrow(() -> new RuntimeException());

        return findStudy.getChatRooms().stream()
                .map(chatRoom -> ChatRoomResponse.from(chatRoom))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudyResponse> findByUserId(Long userId) {
        List<Study> findStudies = studyQueryRepository.findByUserId(userId);
        return findStudies.stream()
                .map(study -> StudyResponse.fromWithTag(study))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudyResponse> findByWaitUserId(Long userId) {
        List<Study> findStudies = studyQueryRepository.findWithWaitUserByUserId(userId);
        return findStudies.stream()
                .map(study -> StudyResponse.fromWithWaitUserAndTag(study))
                .collect(Collectors.toList());
    }
}
