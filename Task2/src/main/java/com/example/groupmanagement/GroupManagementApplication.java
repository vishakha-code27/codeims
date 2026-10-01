package com.example.groupmanagement;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class GroupManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(GroupManagementApplication.class, args);
    }
}

@Entity
@Table(name = "customer_group")
class GroupModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Integer groupId;

    @Column(name = "group_name", nullable = false, unique = true, length = 255)
    @NotBlank(message = "Group name cannot be blank")
    private String groupName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

@Repository
interface GroupRepository extends JpaRepository<GroupModel, Integer> {
    List<GroupModel> findByIsActiveTrue();
    Optional<GroupModel> findByGroupNameIgnoreCase(String groupName);
}

@Entity
@Table(name = "chain", uniqueConstraints = @UniqueConstraint(name = "uk_chain_gstn_no", columnNames = "gstn_no"))
class ChainModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chain_id")
    private Integer chainId;

    @Column(name = "company_name", nullable = false, length = 255)
    @NotBlank(message = "Company name cannot be blank")
    @Size(max = 255, message = "Company name cannot exceed 255 characters")
    private String companyName;

    @Column(name = "gstn_no", nullable = false, unique = true, length = 15)
    @NotBlank(message = "GSTN number cannot be blank")
    @Pattern(regexp = "[A-Z0-9]{15}", message = "GSTN number must contain 15 letters or digits")
    private String gstnNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupModel group;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getChainId() { return chainId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getGstnNo() { return gstnNo; }
    public void setGstnNo(String gstnNo) { this.gstnNo = gstnNo; }
    public GroupModel getGroup() { return group; }
    public void setGroup(GroupModel group) { this.group = group; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

@Repository
interface ChainRepository extends JpaRepository<ChainModel, Integer> {
    List<ChainModel> findByIsActiveTrueOrderByCreatedAtDesc();
    List<ChainModel> findByGroup_GroupIdAndIsActiveTrueOrderByCreatedAtDesc(Integer groupId);
    boolean existsByGstnNoIgnoreCase(String gstnNo);
}

@Controller
class GroupController {
    private final GroupRepository groupRepository;
    private final ChainRepository chainRepository;

    public GroupController(GroupRepository groupRepository, ChainRepository chainRepository) {
        this.groupRepository = groupRepository;
        this.chainRepository = chainRepository;
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) Integer groupId, Model model) {
        List<GroupModel> activeGroups = groupRepository.findByIsActiveTrue();
        List<ChainModel> activeChains = groupId == null
                ? chainRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                : chainRepository.findByGroup_GroupIdAndIsActiveTrueOrderByCreatedAtDesc(groupId);
        model.addAttribute("groups", activeGroups);
        model.addAttribute("totalGroups", activeGroups.size());
        model.addAttribute("chains", activeChains);
        model.addAttribute("totalChains", activeChains.size());
        model.addAttribute("selectedGroupId", groupId);
        return "dashboard";
    }

    @GetMapping("/chains/add")
    public String addChainForm(Model model) {
        model.addAttribute("groups", groupRepository.findByIsActiveTrue());
        return "add_chain";
    }

    @PostMapping("/chains/add")
    public String addChain(@RequestParam(required = false) String companyName,
                           @RequestParam(required = false) String gstnNo,
                           @RequestParam(required = false) Integer groupId,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        String trimmedCompanyName = companyName == null ? "" : companyName.trim();
        String normalizedGstn = gstnNo == null ? "" : gstnNo.trim().toUpperCase();
        Optional<GroupModel> group = groupId == null ? Optional.empty() : groupRepository.findById(groupId);

        String error = null;
        if (trimmedCompanyName.isEmpty() || normalizedGstn.isEmpty() || groupId == null) {
            error = "Company name, GSTN number, and group are required.";
        } else if (trimmedCompanyName.length() > 255) {
            error = "Company name cannot exceed 255 characters.";
        } else if (!normalizedGstn.matches("[A-Z0-9]{15}")) {
            error = "GSTN number must contain exactly 15 letters or digits.";
        } else if (group.isEmpty() || !Boolean.TRUE.equals(group.get().getIsActive())) {
            error = "Select an active group.";
        } else if (chainRepository.existsByGstnNoIgnoreCase(normalizedGstn)) {
            error = "This GSTN number is already registered.";
        }

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("groups", groupRepository.findByIsActiveTrue());
            model.addAttribute("companyName", companyName);
            model.addAttribute("gstnNo", gstnNo);
            model.addAttribute("selectedGroupId", groupId);
            return "add_chain";
        }

        ChainModel chain = new ChainModel();
        chain.setCompanyName(trimmedCompanyName);
        chain.setGstnNo(normalizedGstn);
        chain.setGroup(group.get());
        chain.setIsActive(true);
        chainRepository.save(chain);

        redirectAttributes.addFlashAttribute("success", "Company added successfully.");
        return "redirect:/";
    }

    @GetMapping("/add")
    public String addGroupForm() {
        return "add_group";
    }

    @PostMapping("/add")
    public String addGroup(@RequestParam("groupName") String groupName, RedirectAttributes redirectAttributes) {
        String trimmedName = groupName != null ? groupName.trim() : "";

        if (trimmedName.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group name cannot be blank!");
            return "redirect:/add";
        }

        if (groupRepository.findByGroupNameIgnoreCase(trimmedName).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "This group name already exists. Use a unique name!");
            return "redirect:/add";
        }

        GroupModel group = new GroupModel();
        group.setGroupName(trimmedName);
        group.setIsActive(true);
        groupRepository.save(group);

        redirectAttributes.addFlashAttribute("success", "Group added successfully!");
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String editGroupForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<GroupModel> groupOpt = groupRepository.findById(id);
        if (groupOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group not found!");
            return "redirect:/";
        }
        model.addAttribute("group", groupOpt.get());
        return "edit_group";
    }

    @PostMapping("/edit/{id}")
    public String editGroup(@PathVariable("id") Integer id, @RequestParam("groupName") String groupName, RedirectAttributes redirectAttributes) {
        String trimmedName = groupName != null ? groupName.trim() : "";

        if (trimmedName.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group name cannot be blank!");
            return "redirect:/edit/" + id;
        }

        Optional<GroupModel> existing = groupRepository.findByGroupNameIgnoreCase(trimmedName);
        if (existing.isPresent() && !existing.get().getGroupId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "This group name is already assigned to another group!");
            return "redirect:/edit/" + id;
        }

        Optional<GroupModel> groupOpt = groupRepository.findById(id);
        if (groupOpt.isPresent()) {
            GroupModel group = groupOpt.get();
            group.setGroupName(trimmedName);
            groupRepository.save(group);
            redirectAttributes.addFlashAttribute("success", "Group updated successfully!");
        }

        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String deleteGroup(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        Optional<GroupModel> groupOpt = groupRepository.findById(id);
        if (groupOpt.isPresent()) {
            GroupModel group = groupOpt.get();
            group.setIsActive(false);
            groupRepository.save(group);
            redirectAttributes.addFlashAttribute("success", "Group deactivated successfully!");
        }
        return "redirect:/";
    }
}
