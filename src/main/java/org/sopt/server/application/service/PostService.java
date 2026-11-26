package org.sopt.server.application.service;

import org.sopt.server.application.event.MemberWithdrawnEvent;
import org.sopt.server.application.port.in.MemberUseCase;
import org.sopt.server.application.port.in.PostUseCase;
import org.sopt.server.application.port.out.PostRepository;
import org.sopt.server.domain.Category;
import org.sopt.server.domain.Member;
import org.sopt.server.domain.Post;
import org.sopt.server.domain.exception.BaseException;
import org.sopt.server.domain.exception.PostErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostService implements PostUseCase {
  private static final int MAX_PAGE_SIZE = 100;
  private static final Sort LATEST =
      Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

  private final PostRepository postRepository;
  private final MemberUseCase memberUseCase;

  public PostService(PostRepository postRepository, MemberUseCase memberUseCase) {
    this.postRepository = postRepository;
    this.memberUseCase = memberUseCase;
  }

  @Override
  public long createPost(String title, String content, Category category, Long memberId) {
    return postRepository.save(new Post(title, content, category, findAuthor(memberId))).getId();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Post> getPosts(int page, int size) {
    if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
      throw new BaseException(PostErrorCode.INVALID_PAGE_REQUEST);
    }
    return postRepository.findAllWithAuthor(PageRequest.of(page, size, LATEST));
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
  public void updatePost(
      long id, long memberId, String title, String content, Category category) {
    Post post = findOwnPostForUpdate(id, memberId);
    post.update(title, content, category);
  }

  @Override
  public void deletePost(long id, long memberId) {
    findOwnPostForUpdate(id, memberId).delete();
  }

  private Post findOwnPostForUpdate(long id, long memberId) {
    Post post = findPostForUpdate(id);
    if (!post.isWrittenBy(memberId)) {
      throw new BaseException(PostErrorCode.NOT_POST_AUTHOR);
    }
    return post;
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
    return memberUseCase.getMember(memberId);
  }

  @EventListener
  public void deleteAllByWithdrawnAuthor(MemberWithdrawnEvent event) {
    postRepository.deleteAllByAuthorId(event.memberId(), event.withdrawnAt());
  }
}
