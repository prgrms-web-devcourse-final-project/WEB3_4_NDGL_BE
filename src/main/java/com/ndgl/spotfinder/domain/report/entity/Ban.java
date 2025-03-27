package com.ndgl.spotfinder.domain.report.entity;

import java.time.LocalDate;

import com.ndgl.spotfinder.domain.report.test.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Ban {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	User user;

	@Column(nullable = false)
	LocalDate startDate;

	@Column(nullable = false)
	LocalDate endDate;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	ReportType banType;

	@Builder
	private Ban(User user, LocalDate startDate, LocalDate endDate, ReportType banType) {
		this.user = user;
		this.startDate = startDate;
		this.endDate = endDate;
		this.banType = banType;
	}

	public static LocalDate calculateEndDate(BanDuration banDuration) {
		LocalDate now = LocalDate.now();
		return switch(banDuration) {
			case ONE_DAY -> now.plusDays(1);
			case ONE_WEEK -> now.plusWeeks(1);
			case ONE_MONTH -> now.plusMonths(1);
			case ONE_YEAR -> now.plusYears(1);
			case PERMANENT -> now.plusYears(1000);
		};
	}
}
