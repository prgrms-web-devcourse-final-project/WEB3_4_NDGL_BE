package com.ndgl.spotfinder.global.initData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import com.ndgl.spotfinder.domain.comment.entity.PostComment;
import com.ndgl.spotfinder.domain.comment.repository.PostCommentRepository;
import com.ndgl.spotfinder.domain.like.entity.Like;
import com.ndgl.spotfinder.domain.like.entity.Like.TargetType;
import com.ndgl.spotfinder.domain.like.repository.LikeRepository;
import com.ndgl.spotfinder.domain.post.entity.Hashtag;
import com.ndgl.spotfinder.domain.post.entity.Location;
import com.ndgl.spotfinder.domain.post.entity.Post;
import com.ndgl.spotfinder.domain.post.entity.PostStatus;
import com.ndgl.spotfinder.domain.post.repository.PostRepository;
import com.ndgl.spotfinder.domain.user.entity.Oauth;
import com.ndgl.spotfinder.domain.user.entity.Provider;
import com.ndgl.spotfinder.domain.user.entity.User;
import com.ndgl.spotfinder.domain.user.repository.OauthRepository;
import com.ndgl.spotfinder.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("dev")
public class BaseInitData {

	// 데이터 생성 개수 설정
	private static final int USER_COUNT = 5;               		// 생성할 사용자 수
	private static final int POSTS_PER_USER_MIN = 1;       		// 사용자당 최소 포스트 수
	private static final int POSTS_PER_USER_MAX = 3;       		// 사용자당 최대 포스트 수
	private static final int COMMENTS_PER_POST_MIN = 1;    		// 포스트당 최소 댓글 수
	private static final int COMMENTS_PER_POST_MAX = 3;    		// 포스트당 최대 댓글 수
	private static final double REPLY_PROBABILITY = 1;      	// 댓글에 대댓글이 달릴 확률
	private static final double POST_LIKE_PROBABILITY = 0.4; 	// 포스트에 좋아요 확률
	private static final double COMMENT_LIKE_PROBABILITY = 0.2; // 댓글에 좋아요 확률

	private final UserRepository userRepository;
	private final OauthRepository oauthRepository;
	private final PostRepository postRepository;
	private final LikeRepository likeRepository;
	private final PostCommentRepository postCommentRepository;

	@Autowired
	@Lazy
	private BaseInitData self;

	@Bean
	public ApplicationRunner initDataApplicationRunner() {
		return args -> {
			log.info("테스트 데이터 초기화 시작");

			self.createUsers();
			self.createPosts();
			self.createComments();
			self.createLikes();
			self.printStatistics();
		};
	}

	@Transactional
	public void createUsers() {
		if (userRepository.count() > 0) {
			log.info("사용자 데이터가 이미 존재하여 초기화를 건너뜁니다.");
			return;
		}

		log.info("사용자 데이터 생성 시작: {} 명", USER_COUNT);

		// 닉네임 및 블로그 이름 목록
		String[] nickNames = {"여행마스터", "맛집탐험가", "자연사랑꾼", "도시여행자", "사진작가", "문화관광러",
			"산책러버", "카페덕후", "건축탐방가", "역사연구자"};

		String[] blogNames = {"여행의 모든 것", "맛집 탐방 일지", "자연과 함께하는 시간", "도시 구석구석 탐방기",
			"프레임 속 세상", "문화가 있는 곳", "산책하는 일상", "카페의 재발견",
			"건축과 여행", "역사 속 여행"};

		// USER_COUNT만큼 사용자 생성
		IntStream.range(0, USER_COUNT).forEach(i -> {
			String email = "test" + (i + 1) + "@example.com";
			String nickName = i < nickNames.length ? nickNames[i] : "사용자" + (i + 1);
			String blogName = i < blogNames.length ? blogNames[i] : "블로그" + (i + 1);

			createAndSaveUser(email, nickName, blogName);
		});

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
			.provider(Provider.GOOGLE)
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
			{"인사동", "서울특별시 종로구 인사동길", "37.5749", "126.9839"},
			{"광화문광장", "서울특별시 종로구 세종로", "37.5725", "126.9768"},
			{"명동", "서울특별시 중구 명동길", "37.5633", "126.9821"},
			{"동대문디자인플라자", "서울특별시 중구 을지로 281", "37.5668", "127.0093"},
			{"청계천", "서울특별시 종로구 청계천로", "37.5696", "126.9789"},
			{"이태원", "서울특별시 용산구 이태원로", "37.5348", "126.9941"},
			{"강남역", "서울특별시 강남구 강남대로", "37.4980", "127.0276"},
			{"코엑스", "서울특별시 강남구 영동대로 513", "37.5115", "127.0593"},
			{"여의도공원", "서울특별시 영등포구 여의공원로", "37.5256", "126.9248"},
			{"올림픽공원", "서울특별시 송파구 올림픽로 424", "37.5201", "127.1238"},
			{"서울숲", "서울특별시 성동구 뚝섬로 273", "37.5446", "127.0379"}
		};

		// 해시태그 데이터
		List<String> tags = List.of("서울여행", "주말나들이", "데이트코스", "가족여행", "맛집투어",
			"감성카페", "역사탐방", "도시산책", "사진명소", "전통문화",
			"야경투어", "쇼핑명소", "힐링스팟", "문화체험", "자연명소");

		// 포스트 주제 데이터
		String[][] themes = {
			{"역사 탐방", "서울의 역사적 명소를 방문하는 코스입니다. 고궁과 박물관을 중심으로 돌아보세요."},
			{"맛집 투어", "서울의 유명 맛집들을 모아봤습니다. 다양한 음식을 즐겨보세요."},
			{"자연 산책", "도심 속 자연을 즐길 수 있는 코스입니다. 힐링하기 좋은 장소들을 소개합니다."},
			{"문화 체험", "한국의 전통문화를 체험할 수 있는 장소들입니다. 특별한 경험을 해보세요."},
			{"야경 명소", "서울의 아름다운 야경을 감상할 수 있는 장소들입니다. 로맨틱한 시간을 보내세요."},
			{"쇼핑 코스", "서울의 쇼핑 명소들을 모아봤습니다. 다양한 상품을 구경하고 쇼핑을 즐겨보세요."},
			{"힐링 여행", "도시 속 휴식 공간들을 소개합니다. 바쁜 일상에서 벗어나 여유로운 시간을 가져보세요."},
			{"가족 여행", "아이들과 함께 즐길 수 있는 장소들입니다. 가족 모두가 즐거운 시간을 보내세요."},
			{"봄 나들이", "봄꽃과 함께하는 서울의 명소들입니다. 따스한 봄 햇살 아래 산책해보세요."},
			{"여름 피서", "더운 여름을 시원하게 보낼 수 있는 장소들입니다. 무더위를 피해 즐겨보세요."}
		};

		// 썸네일 이미지 URL 예시 목록 (실제 프로젝트에 맞게 수정 필요)
		String[] thumbnails = {
			"https://example.com/images/thumbnail1.jpg",
			"https://example.com/images/thumbnail2.jpg",
			"https://example.com/images/thumbnail3.jpg",
			"https://example.com/images/thumbnail4.jpg",
			"https://example.com/images/thumbnail5.jpg"
		};

		Random random = new Random();
		int totalPosts = 0;

		for (User user : users) {
			// 각 사용자마다 랜덤하게 포스트 생성 (POSTS_PER_USER_MIN ~ POSTS_PER_USER_MAX 개)
			int postCount = random.nextInt(POSTS_PER_USER_MAX - POSTS_PER_USER_MIN + 1) + POSTS_PER_USER_MIN;

			for (int j = 0; j < postCount; j++) {
				// 테마 선택
				String[] theme = themes[random.nextInt(themes.length)];
				String title = theme[0] + " - " + (j + 1);
				String content = theme[1];

				// 썸네일 선택
				String thumbnail = thumbnails[random.nextInt(thumbnails.length)];

				// Post 엔티티 생성 - Post.createTempPost를 사용하지 않고 직접 생성
				Post post = Post.builder()
					.title(title)
					.content(content)
					.thumbnail(thumbnail)
					.user(user)
					.status(PostStatus.PUBLIC) // 공개 상태로 설정
					.build();

				// 저장 후 ID 발급 받기
				Post savedPost = postRepository.save(post);

				// 해시태그 추가 (3~5개)
				int hashtagCount = random.nextInt(3) + 3;
				List<String> selectedTags = new ArrayList<>();

				for (int k = 0; k < hashtagCount; k++) {
					String tag = tags.get(random.nextInt(tags.size()));
					if (!selectedTags.contains(tag)) {
						selectedTags.add(tag);
						Hashtag hashtag = Hashtag.builder()
							.name(tag)
							.build();
						savedPost.addHashtag(hashtag); // Post 엔티티의 메서드 사용
					}
				}

				// 장소 추가 (1~3개)
				int locationCount = random.nextInt(3) + 1;
				List<Integer> selectedPlaceIndexes = new ArrayList<>();

				for (int k = 0; k < locationCount; k++) {
					int placeIndex = random.nextInt(places.length);
					if (!selectedPlaceIndexes.contains(placeIndex)) {
						selectedPlaceIndexes.add(placeIndex);
						String[] place = places[placeIndex];

						Location location = Location.builder()
							.name(place[0])
							.address(place[1])
							.latitude(Double.parseDouble(place[2]))
							.longitude(Double.parseDouble(place[3]))
							.build();

						savedPost.addLocation(location); // Post 엔티티의 메서드 사용
					}
				}

				// 변경사항 저장
				postRepository.save(savedPost);
				totalPosts++;
			}
		}

		log.debug("게시물 {}개 생성 완료", totalPosts);
	}

	@Transactional
	public void createComments() {
		if (postCommentRepository.count() > 0) {
			log.info("댓글 데이터가 이미 존재하여 초기화를 건너뜁니다.");
			return;
		}
		log.info("댓글 데이터 생성 시작");

		List<User> users = userRepository.findAll();
		if (users.isEmpty()) {
			log.error("사용자 데이터가 존재하지 않아 댓글을 생성할 수 없습니다.");
			return;
		}

		List<Post> posts = postRepository.findAll();
		if (posts.isEmpty()) {
			log.error("게시글 데이터가 존재하지 않아 댓글을 생성할 수 없습니다.");
			return;
		}

		Random random = new Random();
		String[] commentTemplates = {
			"정말 좋은 정보네요! 감사합니다.",
			"저도 이 장소 다녀왔는데 정말 좋았어요.",
			"사진이 너무 예쁘네요. 어떤 카메라 쓰시나요?",
			"다음 주말에 여기 가볼 계획이에요.",
			"추천해주신 코스 따라 가봤는데 대만족이었습니다!",
			"이런 숨은 명소가 있었다니 놀랍네요.",
			"글 잘 봤습니다. 유익한 정보 감사해요.",
			"장소 추천 감사합니다. 맛집도 같이 알려주시면 더 좋을 것 같아요.",
			"저도 이 장소 좋아하는데, 소개해주셔서 반가워요.",
			"다음에는 어떤 장소를 소개해주실 건가요? 기대됩니다."
		};

		String[] replyTemplates = {
			"의견 감사합니다! 다음에 더 좋은 정보로 찾아올게요.",
			"좋게 봐주셔서 감사합니다.",
			"소니 A7III 카메라로 찍었어요!",
			"방문하시면 정말 좋으실 거예요!",
			"만족하셨다니 다행이네요. 다음에 또 좋은 정보 공유할게요.",
			"네, 많이 알려지지 않은 곳인데 정말 괜찮죠.",
			"감사합니다. 계속 좋은 정보 공유하겠습니다.",
			"다음 포스트에서는 맛집도 같이 소개할게요!",
			"저도 반가워요! 자주 놀러오세요.",
			"다음 주제는 [계절별 추천 코스]입니다. 기대해주세요!"
		};

		int totalComments = 0;
		int totalReplies = 0;

		// 각 포스트마다 댓글 생성
		for (Post post : posts) {
			int commentCount =
				random.nextInt(COMMENTS_PER_POST_MAX - COMMENTS_PER_POST_MIN + 1) + COMMENTS_PER_POST_MIN;
			List<PostComment> postComments = new ArrayList<>();

			for (int i = 0; i < commentCount; i++) {
				// 랜덤 사용자 선택
				User randomUser = users.get(random.nextInt(users.size()));
				String commentContent = commentTemplates[random.nextInt(commentTemplates.length)];

				PostComment comment = PostComment.builder()
					.content(commentContent)
					.user(randomUser)
					.post(post)
					.likeCount(0L)
					.build();

				PostComment savedComment = postCommentRepository.save(comment);
				postComments.add(savedComment);
				totalComments++;
			}

			// 댓글 중 일부에 대댓글 추가
			for (PostComment parentComment : postComments) {
				if (random.nextDouble() < REPLY_PROBABILITY) {
					// 대댓글 작성자 선택
					User replyUser = random.nextBoolean() ? post.getUser() : users.get(random.nextInt(users.size()));
					String replyContent = replyTemplates[random.nextInt(replyTemplates.length)];

					PostComment reply = PostComment.builder()
						.content(replyContent)
						.user(replyUser)
						.post(post)
						.parentComment(parentComment)
						.likeCount(0L)
						.build();

					postCommentRepository.save(reply);
					totalReplies++;
				}
			}
		}

		log.debug("댓글 {}개, 대댓글 {}개 생성 완료", totalComments, totalReplies);
	}

	@Transactional
	public void createLikes() {
		if (likeRepository.count() > 0) {
			log.info("좋아요 데이터가 이미 존재하여 초기화를 건너뜁니다.");
			return;
		}

		log.info("좋아요 데이터 생성 시작");

		List<User> users = userRepository.findAll();
		if (users.isEmpty()) {
			log.error("사용자 데이터가 존재하지 않아 좋아요를 생성할 수 없습니다.");
			return;
		}

		List<Post> posts = postRepository.findAll();
		if (posts.isEmpty()) {
			log.error("게시물 데이터가 존재하지 않아 좋아요를 생성할 수 없습니다.");
			return;
		}

		List<PostComment> comments = postCommentRepository.findAll();

		Random random = new Random();
		int totalLikes = 0;

		// 포스트 좋아요 생성
		for (User user : users) {
			for (Post post : posts) {
				// 자신의 포스트는 제외
				if (!post.getUser().equals(user) && random.nextDouble() < POST_LIKE_PROBABILITY) {
					Like like = Like.builder()
						.user(user)
						.targetId(post.getId())
						.targetType(TargetType.POST)
						.build();

					likeRepository.save(like);
					post.updateLikeCount(1); // Post 엔티티의 메서드 사용
					postRepository.save(post); // 변경사항 저장
					totalLikes++;
				}
			}
		}

		// 댓글 좋아요 생성
		for (User user : users) {
			for (PostComment comment : comments) {
				// 자신의 댓글은 제외
				if (!comment.getUser().equals(user) && random.nextDouble() < COMMENT_LIKE_PROBABILITY) {
					Like like = Like.builder()
						.user(user)
						.targetId(comment.getId())
						.targetType(TargetType.COMMENT)
						.build();

					likeRepository.save(like);
					comment.updateLikeCount(1); // Comment 엔티티의 메서드 사용
					postCommentRepository.save(comment); // 변경사항 저장
					totalLikes++;
				}
			}
		}

		log.debug("좋아요 {} 개 생성 완료", totalLikes);
	}

	@Transactional(readOnly = true)
	public void printStatistics() {
		log.info("테스트 데이터 초기화 완료");
		log.info("===== 생성된 데이터 통계 =====");
		log.info("사용자: {}명", userRepository.count());
		log.info("OAuth 연결: {}개", oauthRepository.count());
		log.info("게시물: {}개", postRepository.count());
		log.info("좋아요: {}개", likeRepository.count());
		log.info("댓글: {}개", postCommentRepository.count());
		log.info("===========================");
	}
}