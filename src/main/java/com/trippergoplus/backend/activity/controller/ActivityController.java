package com.trippergoplus.backend.activity.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.trippergoplus.backend.activity.model.Activity;
import com.trippergoplus.backend.activity.model.ActivityImage;
import com.trippergoplus.backend.activity.model.ActivityLocation;
import com.trippergoplus.backend.activity.service.ActivityService;


@Controller
public class ActivityController {

	@Autowired
	private ActivityService aService;

	@GetMapping("/activityquery")
	public String getActivitiesByFiltersAndLocationAndDate(
			@RequestParam(value = "location", required = false) ActivityLocation location,
			@RequestParam(value = "validfrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate validfrom,
			@RequestParam(value = "validto", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate validto,
			@RequestParam(value = "tourCategories", required = false) List<String> tourCategories,
			@RequestParam(value = "prices", required = false) List<String> prices, Model model) {

		List<Activity> filteredActivities = new ArrayList<>();

		if (location != null && validfrom != null && validto != null) {
			// 如果提供了地点和日期，则进行地点和日期的查询
			List<Activity> activitiesByLocationAndDate = aService.findByLocationAndDateRange(location, validfrom,
					validto);
			// 根据其他筛选条件进行进一步筛选
			if (!activitiesByLocationAndDate.isEmpty()) {
				if ((tourCategories != null && !tourCategories.isEmpty()) || (prices != null && !prices.isEmpty())) {
					// 使用其他筛选条件进行进一步筛选
					if (tourCategories != null && !tourCategories.isEmpty() && prices != null && !prices.isEmpty()) {
						// 同时根据活动分类和价格范围进行筛选
						filteredActivities = aService.findByTourCategoriesAndPriceRangeAndLocationAndDate(
								tourCategories, prices, location, validfrom, validto);
					} else if (tourCategories != null && !tourCategories.isEmpty()) {
						// 根据活动分类进行筛选
						filteredActivities = aService.findByTourCategoriesAndLocationAndDate(tourCategories, location,
								validfrom, validto);
					} else {
						// 根据价格范围进行筛选
						filteredActivities = aService.findByPriceRangeAndLocationAndDate(prices, location, validfrom,
								validto);
					}
				} else {
					// 如果未指定其他筛选条件，则直接使用地点和日期的查询结果
					filteredActivities = activitiesByLocationAndDate;
				}
			}
		} else {
			// 如果没有提供地点和日期，则直接根据其他筛选条件进行查询
			if ((tourCategories != null && !tourCategories.isEmpty()) || (prices != null && !prices.isEmpty())) {
				// 使用其他筛选条件进行查询
				// System.out.println("1"+prices);
				if (tourCategories != null && !tourCategories.isEmpty() && prices != null && !prices.isEmpty()) {
					// 同时根据活动分类和价格范围进行查询
					filteredActivities = aService.findByTourCategoriesAndPriceRange(tourCategories, prices);
					// System.out.println("2"+prices);
				} else if (tourCategories != null && !tourCategories.isEmpty()) {
					// 根据活动分类进行查询
					filteredActivities = aService.findByTourCategories(tourCategories);
					// System.out.println("3"+prices);
				} else {
					// 根据价格范围进行查询
					filteredActivities = aService.findByPriceRange(prices);
					System.out.println("4" + prices);
				}
			} else {
				// 如果没有提供任何筛选条件，则可能执行默认操作，这里示例返回所有活动
				filteredActivities = aService.findAll();
			}
		}

		model.addAttribute("activities", filteredActivities);
		return "/backend/activity/activityQueryAllFront";
	}

	@GetMapping("/activityqueryallfront")
	public String getAllActivitiesFront(Model model) {
		List<Activity> allActivities = aService.findAll();
		for (Activity activity : allActivities) {
			List<ActivityImage> activityImages = activity.getActivityImages();
			System.out.println(activityImages);
		}
		model.addAttribute("activities", allActivities);

		return "/backend/activity/activityQueryAllFront";
	}

	@GetMapping("/activityqueryfront")
	public String getActivityDetails(@RequestParam("tourId") Integer tourId, Model model) {
		Activity activity = aService.findById(tourId);
		if (activity != null) {
			List<ActivityImage> activityImages = activity.getActivityImages();
			System.out.println(activityImages);
			model.addAttribute("activity", activity);
			model.addAttribute("activityImages", activityImages);

			return "/backend/activity/activityQueryFront";
		} else {
			return "redirect:/error";
		}
	}

//	後端加分頁
//	@GetMapping("/activityqueryall")
//	public String processQueryAll(@RequestParam(defaultValue = "0") int page,
//			@RequestParam(defaultValue = "10") int size, Model model) {
//		Page<Activity> activityPage = aService.findAllByPage(PageRequest.of(page, size));
//		model.addAttribute("activities", activityPage.getContent());
//		model.addAttribute("currentPage", page);
//		model.addAttribute("totalPages", activityPage.getTotalPages());
//		return "/backend/activity/activityQueryAll";
//	}

	@GetMapping("/activityqueryall")
	public String processQueryAll(Model model) {
		List<Activity> allActivities = aService.findAll();
		model.addAttribute("activities", allActivities);
		return "/backend/activity/activityQueryAll";
	}

	@DeleteMapping("/activity/delete/{id}")
	public ResponseEntity<String> deleteActivity(@PathVariable("id") Integer id) {
		aService.deleteById(id);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/activity/insert")
	public String createActivity(@ModelAttribute("activity") Activity activity,
			@RequestParam("imageFiles") List<MultipartFile> imageFiles) {
		aService.insert(activity, imageFiles);
		return "redirect:/activityqueryall";
	}

	@GetMapping("/activity/edit/{id}")
	public String showEditForm(@PathVariable("id") Integer id, Model model) {
		System.out.println(id);
		Activity activity = aService.findById(id);
		model.addAttribute("activity", activity);
		return "/backend/activity/editActivity";
	}

	@PostMapping("/activity/update/{tourID}")
	public String updateActivity(@PathVariable("tourID") Integer tourID, @ModelAttribute Activity activity) {
		activity.setTourID(tourID);
		aService.update(activity);
		return "redirect:/activityqueryall";
	}

//	@GetMapping("/activityquery")
//	public String getActivitiesByFilters(
//			@RequestParam(value = "tourCategories", required = false) List<String> tourCategories,
//			@RequestParam(value = "prices", required = false) List<String> prices, Model model) {
//		// System.out.println(prices);
//		System.out.println(tourCategories);
//		// 檢查是否有傳遞任何篩選條件
//		if ((tourCategories != null && !tourCategories.isEmpty()) || (prices != null && !prices.isEmpty())) {
//			// 使用篩選條件查詢活動
//			List<Activity> activities;
//
//			if (tourCategories != null && !tourCategories.isEmpty() && prices != null && !prices.isEmpty()) {
//				// 同時根據活動類別和價格範圍進行查詢
//				activities = aService.findByTourCategoriesAndPriceRange(tourCategories, prices);
//			} else if (tourCategories != null && !tourCategories.isEmpty()) {
//				// 根據活動類別進行查詢
//				activities = aService.findByTourCategories(tourCategories);
//			} else {
//				// 根據價格範圍進行查詢
//				activities = aService.findByPriceRange(prices);
//			}
//			model.addAttribute("activities", activities);
//		} else {
//			// 如果未指定任何篩選條件，則可能執行默認操作或返回錯誤信息，視需求而定
//			// 這裡示例返回所有活動，你可以更改為其他默認操作
//			List<Activity> activities = aService.findAll();
//			model.addAttribute("activities", activities);
//		}
//		return "/backend/activity/activityQueryAllFront";
//	}
//
//	@GetMapping("/activityquerybylocationanddate")
//	public String getActivitiesByLocationAndDate(@RequestParam("location") ActivityLocation location,
//			@RequestParam("validfrom") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate validfrom,
//			@RequestParam("validto") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate validto, Model model) {
//		List<Activity> activities = aService.findByLocationAndDateRange(location, validfrom, validto);
//		model.addAttribute("activities", activities);
//		return "/backend/activity/activityQueryAllFront";
//	}
//
//	@GetMapping("/activityquerybycategory")
//	public String getActivitiesByCategory(
//			@RequestParam(value = "tourCategories", required = false) List<String> tourCategories, Model model) {
//		// 檢查是否有傳遞任何篩選條件
//		if (tourCategories != null && !tourCategories.isEmpty()) {
//			// 使用篩選條件查詢活動
//			List<Activity> activities = aService.findByTourCategories(tourCategories);
//			model.addAttribute("activities", activities);
//		} else {
//			// 如果未指定任何篩選條件，則可能執行默認操作或返回錯誤信息，視需求而定
//			// 這裡示例返回所有活動，你可以更改為其他默認操作
//			List<Activity> activities = aService.findAll();
//			model.addAttribute("activities", activities);
//		}
//		return "/backend/activity/activityQueryAllFront";
//	}
//
//	@GetMapping("/activityquerybyprice")
//	public String getActivitiesByPrice(@RequestParam(value = "prices", required = false) List<String> prices,
//			Model model) {
//		System.out.println(prices);
//		// 检查是否有传递任何筛选条件
//		if (prices != null && !prices.isEmpty()) {
//			// 使用价格范围筛选活动
//			List<Activity> activities = aService.findByPriceRange(prices); // 你需要实现该方法
//			model.addAttribute("activities", activities);
//		} else {
//			// 如果未指定任何筛选条件，可以执行默认操作，例如返回所有活动
//			List<Activity> activities = aService.findAll();
//			model.addAttribute("activities", activities);
//		}
//		return "/backend/activity/activityQueryAllFront"; // 返回活动页面
//	}
}
