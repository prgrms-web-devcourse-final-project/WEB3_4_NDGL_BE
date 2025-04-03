package com.ndgl.spotfinder.global.init;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.admin.entity.Admin;
import com.ndgl.spotfinder.domain.admin.repository.AdminRepository;
import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;
import com.ndgl.spotfinder.domain.post.entity.Hashtag;
import com.ndgl.spotfinder.domain.post.entity.Location;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.user.entity.Oauth;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.OauthRepository;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("test") // 로컬, 개발 환경에서만 실행
public class ReportTestInit {

	private final UserRepository userRepository;
	private final OauthRepository oauthRepository;
	private final AdminRepository adminRepository;
	private final PostRepository postRepository;
	private final PostCommentRepository postCommentRepository;

	@Autowired
	@Lazy
	private ReportTestInit self;

	@Bean
	public ApplicationRunner initDataApplicationRunner() {
		return args -> {
			log.info("테스트 데이터 초기화 시작");

			self.createAdmin();
			self.createUsers();
			self.createPosts();
		};
	}

	@Transactional
	public void createAdmin() {
		if (adminRepository.count() > 0) {
			log.info("관리자 데이터가 이미 존재하여 초기화를 건너뜁니다.");
			return;
		}
		// 테스트 사용자 생성
		Admin admin = Admin.builder().username("admin").password("12345").build();
		adminRepository.save(admin);
	}

	@Transactional
	public void createUsers() {
		if (userRepository.count() > 0) {
			log.info("사용자 데이터가 이미 존재하여 초기화를 건너뜁니다.");
			return;
		}

		log.debug("사용자 데이터 생성 시작");

		// 테스트 사용자 생성
		createAndSaveUser("test1@example.com", "테스트유저1", "테스트블로그1");
		createAndSaveUser("test2@example.com", "테스트유저2", "테스트블로그2");
		createAndSaveUser("test3@example.com", "테스트유저3", "테스트블로그3");

		log.debug("사용자 {} 명 생성 완료", userRepository.count());
	}

	private void createAndSaveUser(String email, String nickName, String blogName) {
		User user = User.builder()
			.email(email)
			.nickName(nickName)
			.blogName(blogName)
			.build();

		User savedUser = userRepository.save(user);

		Oauth oauth = Oauth.builder()
			.user(savedUser)
			.provider(Oauth.Provider.GOOGLE)
			.identify("google_" + savedUser.getId())
			.build();

		oauthRepository.save(oauth);
	}

	@Transactional
	public void createPosts() {
		if (postRepository.count() > 0) {
			log.info("게시물 데이터가 이미 존재하여 초기화를 건너뜁니다.");
			return;
		}

		log.info("게시물 데이터 생성 시작");

		List<User> users = userRepository.findAll();
		if (users.isEmpty()) {
			log.warn("사용자 데이터가 없어 게시물을 생성할 수 없습니다.");
			return;
		}

		// 장소 데이터
		String[][] places = {
			{"경복궁", "서울특별시 종로구 사직로 161", "37.5796", "126.9770"},
			{"남산타워", "서울특별시 용산구 남산공원길 105", "37.5511", "126.9882"},
			{"한강공원", "서울특별시 영등포구 여의동로 330", "37.5284", "126.9336"},
			{"북촌한옥마을", "서울특별시 종로구 계동길 37", "37.5825", "126.9856"},
			{"인사동", "서울특별시 종로구 인사동길", "37.5749", "126.9839"}
		};

		// 해시태그 데이터
		List<String> tags = List.of("서울여행", "주말나들이", "데이트코스", "가족여행");

		for (User user : users) {
			String theme = user.getNickName().equals("테스트유저1") ? "역사 탐방" :
				user.getNickName().equals("테스트유저2") ? "맛집 탐방" : "자연 탐방";

			for (int i = 1; i <= 2; i++) {
				Post post = Post.builder()
					.title(user.getNickName() + "의 " + theme + " 코스 " + i)
					.content(theme + "을 소개합니다. 서울의 아름다운 명소들을 방문해보세요.")
					.user(user)
					.thumbnail("https://example.com/" + user.getId() + "_thumbnail" + i + ".jpg")
					.viewCount((long)(i * 10)) // 조회수 설정
					.likeCount(0L)
					.build();

				for (String tag : tags) {
					Hashtag hashtag = Hashtag.builder()
						.name(tag)
						.build();
					post.addHashtag(hashtag);
				}

				// 장소 추가 (사용자별 일관된 장소 선택)
				// 사용자1: 0,1,2 / 사용자2: 1,2,3 / 사용자3: 2,3,4
				int startIdx = Math.min(users.indexOf(user), places.length - 3);
				for (int j = 0; j < 3; j++) {
					int placeIdx = startIdx + j;
					Location location = Location.builder()
						.name(places[placeIdx][0])
						.address(places[placeIdx][1])
						.latitude(Double.parseDouble(places[placeIdx][2]))
						.longitude(Double.parseDouble(places[placeIdx][3]))
						.sequence(j + 1)
						.build();
					post.addLocation(location);
				}

				postRepository.save(post);
				log.debug("게시물 생성: {}", post.getTitle());

				PostComment postComment = PostComment.builder()
					.content("댓글" + 2 * users.indexOf(user) + i)
					.post(post)
					.user(user)
					.likeCount(0L)
					.build();

				postCommentRepository.save(postComment);
			}
		}
	}
}
