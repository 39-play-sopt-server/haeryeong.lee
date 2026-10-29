package org.sopt.server.application.port.out;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.sopt.server.domain.Member;
import org.sopt.server.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
  @Query("select p from Post p join fetch p.author order by p.id")
  List<Post> findAllWithAuthor();

  @Query("select p from Post p join fetch p.author where p.id = :id")
  Optional<Post> findWithAuthorById(@Param("id") long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Post p where p.id = :id")
  Optional<Post> findByIdForUpdate(@Param("id") long id);

  @Modifying(clearAutomatically = true)
  @Query("update Post p set p.viewCount = p.viewCount + 1 where p.id = :id and p.deletedAt is null")
  int increaseViewCount(@Param("id") long id);

  @Modifying
  @Query(
      "update Post p set p.deletedAt = :deletedAt"
          + " where p.author = :author and p.deletedAt is null")
  void deleteAllByAuthor(
      @Param("author") Member author, @Param("deletedAt") LocalDateTime deletedAt);
}
