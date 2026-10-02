package com.manbhavansteel.manbhavan_steel_backend.repository;

import com.manbhavansteel.manbhavan_steel_backend.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {

    @Query(
            value = """
                    SELECT c.*
                    FROM colors c
                    INNER JOIN product_colors pc
                        ON c.id = pc.color_id
                    WHERE pc.product_id = :productId
                    """,
            nativeQuery = true
    )
    List<Color> findColorsByProductId(
            @Param("productId") Long productId
    );

    @Modifying
    @Query(
            value = """
                    DELETE FROM product_colors
                    WHERE product_id = :productId
                    """,
            nativeQuery = true
    )
    void deleteAllProductColors(
            @Param("productId") Long productId
    );

    @Modifying
    @Query(
            value = """
                    INSERT INTO product_colors
                        (product_id, color_id)
                    VALUES
                        (:productId, :colorId)
                    """,
            nativeQuery = true
    )
    void addProductColor(
            @Param("productId") Long productId,
            @Param("colorId") Long colorId
    );
}