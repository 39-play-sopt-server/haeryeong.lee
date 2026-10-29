package org.sopt.server.application.service;

import java.util.List;
import org.sopt.server.application.port.in.PostUseCase;
import org.sopt.server.application.port.out.MemberRepository;
import org.sopt.server.application.port.out.PostRepository;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.Member;
import org.sopt.server.domain.Post;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.MemberErrorCode;
import org.sopt.server.domain.exception.PostErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostService implements PostUseCase {
  private final PostRepository postRepository;
  private final MemberRepository memberRepository;

  public PostService(PostRepository postRepository, MemberRepository memberRepository) {
    this.postRepository = postRepository;
    this.memberRepository = memberRepository;
  }

  @Override
  public long createPost(String title, String content, Category category, Long memberId) {
    return postRepository.save(new Post(title, content, category, findAuthor(memberId))).getId();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Post> getPosts() {
    return postRepository.findAllWithAuthor();
  }

  @Override
  public Post getPost(long id) {
    if (postRepository.increaseViewCount(id) == 0) {
      throw new BaseException(PostErrorCode.POST_NOT_FOUND);
    }
    return postRepository
        .findWithAuthorById(id)
        .orElseThrow(() -> new BaseException(PostErrorCode.POST_NOT_FOUND));
  }

  @Override
  public void updatePost(long id, String title, String content, Category category) {
    Post post = findPostForUpdate(id);
    post.update(title, content, category);
  }

  @Override
  public void deletePost(long id) {
    findPostForUpdate(id).delete();
  }

  private Post findPostForUpdate(long id) {
    return postRepository
        .findByIdForUpdate(id)
        .orElseThrow(() -> new BaseException(PostErrorCode.POST_NOT_FOUND));
  }

  private Member findAuthor(Long memberId) {
    if (memberId == null) {
      throw new BaseException(PostErrorCode.AUTHOR_REQUIRED);
    }
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new BaseException(MemberErrorCode.MEMBER_NOT_FOUND));
  }
}
