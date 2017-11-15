package com.community.web.controller;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.community.web.entity.Question;
import com.community.web.entity.Users;
import com.community.web.service.CommunityService;
import com.community.web.util.VerifyRecaptcha;

@Controller
@RequestMapping("/QA")
public class UserController {

	@Autowired
	private CommunityService communityService;

	@Autowired
	private ServletContext servletContext;

	@Value("#{countriesOption}")
	private Map<String, String> countryOptions;

	@GetMapping("/Users")
	public String showUserCV(@RequestParam("answerUserId") int theId, Model model) {
		Users theUser = communityService.getUserById(theId);
		byte[] encodeBase64 = Base64.encodeBase64(theUser.getPICTURE());
		String base64Encoded = new String(encodeBase64);
		Map<String, String> treeMap = new TreeMap<String, String>(countryOptions);
		model.addAttribute("countriesOption", treeMap);
		model.addAttribute("userImage", base64Encoded);
		model.addAttribute("user", theUser);
		return "user-form";
	}

	@GetMapping("/UserCV")
	public String userCv(@RequestParam("privateId") int theId, Model model) {
		Users users = communityService.getUserById(theId);
		List<Question> questionList = communityService.getQuestionListByUserId(theId);

		byte[] encodeBase64 = Base64.encodeBase64(users.getPICTURE());
		String base64Encoded = new String(encodeBase64);

		model.addAttribute("questionList", questionList);
		model.addAttribute("profilePicture", base64Encoded);
		model.addAttribute("users", users);
		return "user-cv";
	}

	@PostMapping("/RegisterUser")
	public String registerUser(@ModelAttribute("user") Users users, @RequestParam("fileUpload") MultipartFile fileUpload,
			@RequestParam("g-recaptcha-response") String gRecaptchaResponse, HttpServletRequest request,
			RedirectAttributes redirectAttrs) throws Exception {
		
		final String ErrorMessage = "There is an account with that email adress: ";
		
		if(communityService.getUserByEmail(users.getEMAIL()) != null) {
			
			redirectAttrs.addFlashAttribute("error", ErrorMessage + users.getEMAIL());
			return "redirect:SignUp";
			
		}else {
			
			boolean verify = VerifyRecaptcha.verify(gRecaptchaResponse);
			
			if(verify) {
				if (fileUpload.isEmpty()) {
					File theFile = new File(servletContext.getRealPath("/resources/images/nouser.jpg"));
					byte[] array = Files.readAllBytes(theFile.toPath());
					users.setPICTURE(array);
					
				} else {

					users.setPICTURE(fileUpload.getBytes());
				}
				
				communityService.saveUser(users);
				redirectAttrs.addFlashAttribute("message", "Your registration has been completed successfully");
				return "redirect:" + request.getParameter("from");
			}else {
				redirectAttrs.addFlashAttribute("error", "You missed the Captcha!, It must be used or It returned false!");
				return "redirect:SignUp";
			}
		}
		
	}
	
	@PostMapping("/UpdateUser")
	public String saveUser(@ModelAttribute("user") Users users, @RequestParam("fileUpload") MultipartFile fileUpload,
			@RequestParam("g-recaptcha-response") String gRecaptchaResponse, HttpServletRequest request,
			RedirectAttributes redirectAttrs) throws Exception {

		
		boolean verify = VerifyRecaptcha.verify(gRecaptchaResponse);

		if (verify) {
			if (fileUpload.isEmpty()) {
				File theFile = new File(servletContext.getRealPath("/resources/images/nouser.jpg"));
				byte[] array = Files.readAllBytes(theFile.toPath());
				users.setPICTURE(array);
			} else {

				users.setPICTURE(fileUpload.getBytes());
			}

			communityService.saveUser(users);
			redirectAttrs.addFlashAttribute("message", "Your profile updated successfully.");
			return "redirect:" + request.getParameter("from");
		}

		redirectAttrs.addFlashAttribute("message", "You missed the Captcha!, It must be used or It returned false!");
		return "redirect:SignUp";

	}

	@PostMapping("/UpdatePassword")
	public String updatePassword(@ModelAttribute("user") Users theUser, @RequestParam("newPsw") String newPsw,
			@RequestParam("confirmPsw") String confirmPsw, @RequestParam("UUID") String UUIDkey, RedirectAttributes redirectAtt) {

		if (theUser != null) {
			Users usr = communityService.getUserById(theUser.getID());
			
			if (UUIDkey.equalsIgnoreCase(usr.getUUID())) {
				if (newPsw.equals(confirmPsw)) {

					communityService.updateUserPassword(usr.getID(), newPsw);
					redirectAtt.addFlashAttribute("message", "Your password changed successfully.");
					return "redirect:AllQuestions";
				}else {
					redirectAtt.addFlashAttribute("error", "Passwords are doesn't match!");
					return "redirect:Resetpassword?userId="+theUser.getID();
				}
			}else {
				redirectAtt.addFlashAttribute("error", "Invalid or expired unique key");
				return "redirect:Resetpassword?userId="+theUser.getID();
			}
		}
		return null;
	}

	@GetMapping("/Resetpassword")
	public String resetPassword(Model model, @RequestParam("userId") int theId) {
		Users usr = communityService.getUserById(theId);
		model.addAttribute("user", usr);
		return "renew-password";
	}
	
	@PostMapping("/SearchUser")
	public String searchUser(@RequestParam("theSearchUser") String searchName, Model model) {
		List<Users> allUsersList = communityService.searchUser(searchName);
		List<String> pictureList = new ArrayList<>();

		for (Users users : allUsersList) {
			byte[] encodeBase64 = Base64.encodeBase64(users.getPICTURE());
			String base64Encoded = new String(encodeBase64);
			pictureList.add(base64Encoded);
		}

		model.addAttribute("pictureList", pictureList);
		model.addAttribute("userList", allUsersList);
		return "users-list";
	}

	@GetMapping("/Users-list")
	public String usersList(Model model) {
		List<Users> allUsersList = communityService.getUserList();
		List<String> pictureList = new ArrayList<>();

		for (Users users : allUsersList) {
			byte[] encodeBase64 = Base64.encodeBase64(users.getPICTURE());
			String base64Encoded = new String(encodeBase64);
			pictureList.add(base64Encoded);
		}
		model.addAttribute("pictureList", pictureList);
		model.addAttribute("userList", allUsersList);
		return "users-list";
	}

}
