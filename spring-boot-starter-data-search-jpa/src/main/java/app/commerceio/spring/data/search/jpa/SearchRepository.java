package app.commerceio.spring.data.search.jpa;

import app.commerceio.spring.data.search.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface SearchRepository<T, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    Page<T> findAll(String search, Pageable pageable);

    Page<T> findAll(String search, Pageable pageable, Mapper mapper);
}
