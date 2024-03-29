/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.sjc.superhero.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import sg.sjc.superhero.dtos.Organization;
import sg.sjc.superhero.dtos.SuperPerson;
import sg.sjc.superhero.services.OrganizationService;
import sg.sjc.superhero.services.SuperPersonsService;

/**
 *
 * @author stephenespinal
 */
@Controller
public class OrganizationController {

    private OrganizationService orgService;

    private SuperPersonsService superPersonService;
    
    Validator validate = Validation.buildDefaultValidatorFactory().getValidator();

    @Autowired
    public OrganizationController(OrganizationService orgService, SuperPersonsService superPersonService) {
        this.orgService = orgService;
        this.superPersonService = superPersonService;
    }

    @GetMapping("organizations")
    public String loadPage(Model model) {
        List<SuperPerson> superPersonList = superPersonService.getAllSuperPersons();
        List<Organization> organizationList = orgService.readAllOrganizations();
        Set<ConstraintViolation<Organization>> violations = new HashSet<>();

        model.addAttribute("organizationList", organizationList);
        model.addAttribute("superPersonList", superPersonList);
        model.addAttribute("errors", violations);

        return "organizations";
    }

    @PostMapping("addNewOrganization")
    public String addOrganization(HttpServletRequest request, @Valid Organization org) {
        
        Set<ConstraintViolation<Organization>> violations = new HashSet<>();
        violations = validate.validate(org);
        
        
        String[] superPersonIds = request.getParameterValues("members");
        

        List<SuperPerson> superPersons = new ArrayList<SuperPerson>();

        if (superPersonIds != null) {
            for (String superId : superPersonIds) {
                superPersons.add(superPersonService.getSuperPersonById(Integer.parseInt(superId)));
            }
        }

        Organization newOrganization = new Organization();

        newOrganization.setName(request.getParameter("name"));
        newOrganization.setDescription(request.getParameter("description"));
        newOrganization.setContactInfo(request.getParameter("contactInfo"));
        newOrganization.setSuperPersons(superPersons);

        orgService.createOrganization(newOrganization);

        if (superPersonIds != null) {

            for (String superId : superPersonIds) {
                orgService.createNewMember(Integer.parseInt(superId), newOrganization.getOrgId());
            }
        }

        //tell spring to redirect user to mapping locations
        return "redirect:/organizations";
    }

    @GetMapping("deleteOrganization")
    public String deleteOrganization(HttpServletRequest request) {
        int id = Integer.parseInt(request.getParameter("id"));
        orgService.deleteOrgById(id);
        orgService.deleteOrganization(id);

        return "redirect:/organizations";
    }

    @PostMapping("editOrganization")
    @Transactional
    public String editOrganization(HttpServletRequest request, @Valid Organization organizationToEdit, BindingResult result) {
        

        String[] superPersonIds = request.getParameterValues("members");

        List<SuperPerson> superPersons = new ArrayList<SuperPerson>();

        if (superPersonIds != null) {
            for (String superId : superPersonIds) {
                superPersons.add(superPersonService.getSuperPersonById(Integer.parseInt(superId)));
            }
        }


        organizationToEdit.setOrgId(Integer.parseInt(request.getParameter("orgId")));
        organizationToEdit.setName(request.getParameter("name"));
        organizationToEdit.setDescription(request.getParameter("description"));
        organizationToEdit.setContactInfo(request.getParameter("contactInfo"));

        orgService.deleteOrgById(organizationToEdit.getOrgId());
        orgService.updateOrganization(organizationToEdit);

        if (superPersonIds != null) {

            for (String superId : superPersonIds) {
                orgService.createNewMember(Integer.parseInt(superId), organizationToEdit.getOrgId());
            }
        }

        return "redirect:/organizations";
    }

}
