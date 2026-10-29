package org.sopt.server.application.port.out;

import jakarta.persistence.LockModeType;
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

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Post p join fetch p.author where p.id = :id")
  Optional<Post> findByIdForUpdate(@Param("id") long id);

  @Modifying
  @Query("delete from Post p where p.author = :author")
  void deleteAllByAuthor(@Param("author") Member author);
}
