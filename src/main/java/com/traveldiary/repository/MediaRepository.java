package com.traveldiary.repository;

import com.traveldiary.model.Diary;
import com.traveldiary.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByDiary(Diary diary);
} 