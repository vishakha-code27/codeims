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

@Entity
@Table(name = "brand")
class BrandModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Integer brandId;

    @Column(name = "brand_name", nullable = false, length = 50)
    @NotBlank(message = "Brand name cannot be blank")
    @Size(max = 50, message = "Brand name cannot exceed 50 characters")
    private String brandName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chain_id", nullable = false)
    private ChainModel chain;

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
        if (this.isActive == null) this.isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getBrandId() { return brandId; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public ChainModel getChain() { return chain; }
    public void setChain(ChainModel chain) { this.chain = chain; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

@Entity
@Table(name = "zone")
class ZoneModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zone_id")
    private Integer zoneId;

    @Column(name = "zone_name", nullable = false, length = 50)
    @NotBlank(message = "Zone name cannot be blank")
    @Size(max = 50, message = "Zone name cannot exceed 50 characters")
    private String zoneName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private BrandModel brand;

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
        if (this.isActive == null) this.isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getZoneId() { return zoneId; }
    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
    public BrandModel getBrand() { return brand; }
    public void setBrand(BrandModel brand) { this.brand = brand; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

@Repository
interface BrandRepository extends JpaRepository<BrandModel, Integer> {
    List<BrandModel> findByIsActiveTrueOrderByCreatedAtDesc();
}

@Repository
interface ZoneRepository extends JpaRepository<ZoneModel, Integer> {
    boolean existsByBrand_BrandId(Integer brandId);
    List<ZoneModel> findByIsActiveTrueOrderByCreatedAtDesc();
}

@Controller
class GroupController {
    private final GroupRepository groupRepository;
    private final ChainRepository chainRepository;
    private final BrandRepository brandRepository;
    private final ZoneRepository zoneRepository;

    public GroupController(GroupRepository groupRepository, ChainRepository chainRepository,
                           BrandRepository brandRepository, ZoneRepository zoneRepository) {
        this.groupRepository = groupRepository;
        this.chainRepository = chainRepository;
        this.brandRepository = brandRepository;
        this.zoneRepository = zoneRepository;
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) Integer groupId,
                    @RequestParam(required = false) Integer companyId,
                    @RequestParam(required = false) Integer zoneBrandId,
                    @RequestParam(required = false) Integer zoneCompanyId,
                    @RequestParam(required = false) Integer zoneGroupId,
                    Model model) {
        List<GroupModel> activeGroups = groupRepository.findByIsActiveTrue();
        List<ChainModel> allActiveChains = chainRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        List<BrandModel> allActiveBrands = brandRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        List<ZoneModel> activeZones = zoneRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        List<ChainModel> activeChains = groupId == null
            ? allActiveChains
                : chainRepository.findByGroup_GroupIdAndIsActiveTrueOrderByCreatedAtDesc(groupId);
        List<BrandModel> activeBrands = allActiveBrands.stream()
                .filter(brand -> companyId == null || brand.getChain().getChainId().equals(companyId))
                .filter(brand -> groupId == null || brand.getChain().getGroup().getGroupId().equals(groupId))
                .toList();
        List<ZoneModel> filteredZones = activeZones.stream()
            .filter(zone -> zoneBrandId == null || zone.getBrand().getBrandId().equals(zoneBrandId))
            .filter(zone -> zoneCompanyId == null || zone.getBrand().getChain().getChainId().equals(zoneCompanyId))
            .filter(zone -> zoneGroupId == null || zone.getBrand().getChain().getGroup().getGroupId().equals(zoneGroupId))
            .toList();
        model.addAttribute("groups", activeGroups);
        model.addAttribute("totalGroups", activeGroups.size());
        model.addAttribute("chains", activeChains);
        model.addAttribute("totalChains", allActiveChains.size());
        model.addAttribute("brands", activeBrands);
        model.addAttribute("totalBrands", allActiveBrands.size());
        model.addAttribute("totalZones", activeZones.size());
        model.addAttribute("zones", filteredZones);
        model.addAttribute("zoneBrands", allActiveBrands);
        model.addAttribute("brandCompanies", allActiveChains);
        model.addAttribute("selectedGroupId", groupId);
        model.addAttribute("selectedCompanyId", companyId);
        model.addAttribute("selectedZoneBrandId", zoneBrandId);
        model.addAttribute("selectedZoneCompanyId", zoneCompanyId);
        model.addAttribute("selectedZoneGroupId", zoneGroupId);
        return "dashboard";
    }

    @GetMapping("/brands/add")
    public String addBrandForm(Model model) {
        model.addAttribute("companies", chainRepository.findByIsActiveTrueOrderByCreatedAtDesc());
        model.addAttribute("formAction", "/brands/add");
        return "brand_form";
    }

    @PostMapping("/brands/add")
    public String addBrand(@RequestParam(required = false) String brandName,
                           @RequestParam(required = false) Integer chainId,
                           Model model, RedirectAttributes redirectAttributes) {
        String trimmedName = brandName == null ? "" : brandName.trim();
        Optional<ChainModel> chain = chainId == null ? Optional.empty() : chainRepository.findById(chainId);
        if (trimmedName.isEmpty() || trimmedName.length() > 50) {
            model.addAttribute("error", "Brand name is required and cannot exceed 50 characters.");
        } else if (chain.isEmpty() || !Boolean.TRUE.equals(chain.get().getIsActive())) {
            model.addAttribute("error", "Select an active company.");
        }
        if (model.containsAttribute("error")) {
            model.addAttribute("companies", chainRepository.findByIsActiveTrueOrderByCreatedAtDesc());
            model.addAttribute("brandName", brandName);
            model.addAttribute("selectedChainId", chainId);
            return "brand_form";
        }

        BrandModel brand = new BrandModel();
        brand.setBrandName(trimmedName);
        brand.setChain(chain.orElseThrow());
        brand.setIsActive(true);
        brandRepository.save(brand);
        redirectAttributes.addFlashAttribute("success", "Brand added successfully.");
        return "redirect:/#brands";
    }

    @GetMapping("/brands/edit/{id}")
    public String editBrandForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<BrandModel> brand = brandRepository.findById(id);
        if (brand.isEmpty() || !Boolean.TRUE.equals(brand.get().getIsActive())) {
            redirectAttributes.addFlashAttribute("error", "Active brand not found.");
            return "redirect:/#brands";
        }
        model.addAttribute("brand", brand.get());
        model.addAttribute("companies", chainRepository.findByIsActiveTrueOrderByCreatedAtDesc());
        model.addAttribute("selectedChainId", brand.get().getChain().getChainId());
        model.addAttribute("formAction", "/brands/edit/" + id);
        return "brand_form";
    }

    @PostMapping("/brands/edit/{id}")
    public String editBrand(@PathVariable Integer id,
                            @RequestParam(required = false) String brandName,
                            @RequestParam(required = false) Integer chainId,
                            Model model, RedirectAttributes redirectAttributes) {
        Optional<BrandModel> brand = brandRepository.findById(id);
        Optional<ChainModel> chain = chainId == null ? Optional.empty() : chainRepository.findById(chainId);
        String trimmedName = brandName == null ? "" : brandName.trim();
        String error = brand.isEmpty() || !Boolean.TRUE.equals(brand.get().getIsActive())
                ? "Active brand not found."
                : trimmedName.isEmpty() || trimmedName.length() > 50
                    ? "Brand name is required and cannot exceed 50 characters."
                    : chain.isEmpty() || !Boolean.TRUE.equals(chain.get().getIsActive())
                        ? "Select an active company." : null;
        if (error != null) {
            if (brand.isEmpty() || !Boolean.TRUE.equals(brand.get().getIsActive())) {
                redirectAttributes.addFlashAttribute("error", error);
                return "redirect:/#brands";
            }
            model.addAttribute("error", error);
            model.addAttribute("brand", brand.get());
            model.addAttribute("companies", chainRepository.findByIsActiveTrueOrderByCreatedAtDesc());
            model.addAttribute("brandName", brandName);
            model.addAttribute("selectedChainId", chainId);
            model.addAttribute("formAction", "/brands/edit/" + id);
            return "brand_form";
        }

        brand.get().setBrandName(trimmedName);
        brand.get().setChain(chain.get());
        brandRepository.save(brand.get());
        redirectAttributes.addFlashAttribute("success", "Brand updated successfully.");
        return "redirect:/#brands";
    }

    @PostMapping("/brands/delete/{id}")
    public String deleteBrand(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<BrandModel> brand = brandRepository.findById(id);
        if (brand.isEmpty() || !Boolean.TRUE.equals(brand.get().getIsActive())) {
            redirectAttributes.addFlashAttribute("error", "Active brand not found.");
        } else if (zoneRepository.existsByBrand_BrandId(id)) {
            redirectAttributes.addFlashAttribute("error", "This brand is linked to one or more zones and cannot be deleted.");
        } else {
            brand.get().setIsActive(false);
            brandRepository.save(brand.get());
            redirectAttributes.addFlashAttribute("success", "Brand deactivated successfully.");
        }
        return "redirect:/#brands";
    }

    @GetMapping("/zones/add")
    public String addZoneForm(Model model) {
        model.addAttribute("brands", brandRepository.findByIsActiveTrueOrderByCreatedAtDesc());
        model.addAttribute("formAction", "/zones/add");
        return "zone_form";
    }

    @PostMapping("/zones/add")
    public String addZone(@RequestParam(required = false) String zoneName,
                          @RequestParam(required = false) Integer brandId,
                          Model model, RedirectAttributes redirectAttributes) {
        String trimmedName = zoneName == null ? "" : zoneName.trim();
        Optional<BrandModel> brand = brandId == null ? Optional.empty() : brandRepository.findById(brandId);
        String error = trimmedName.isEmpty() || trimmedName.length() > 50
                ? "Zone name is required and cannot exceed 50 characters."
                : brand.isEmpty() || !Boolean.TRUE.equals(brand.get().getIsActive())
                    ? "Select an active brand." : null;
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("zoneName", zoneName);
            model.addAttribute("selectedBrandId", brandId);
            model.addAttribute("brands", brandRepository.findByIsActiveTrueOrderByCreatedAtDesc());
            model.addAttribute("formAction", "/zones/add");
            return "zone_form";
        }

        ZoneModel zone = new ZoneModel();
        zone.setZoneName(trimmedName);
        zone.setBrand(brand.orElseThrow());
        zone.setIsActive(true);
        zoneRepository.save(zone);
        redirectAttributes.addFlashAttribute("success", "Zone added successfully.");
        return "redirect:/#zones";
    }

    @GetMapping("/zones/edit/{id}")
    public String editZoneForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ZoneModel> zone = zoneRepository.findById(id);
        if (zone.isEmpty() || !Boolean.TRUE.equals(zone.get().getIsActive())) {
            redirectAttributes.addFlashAttribute("error", "Active zone not found.");
            return "redirect:/#zones";
        }
        model.addAttribute("zone", zone.get());
        model.addAttribute("brands", brandRepository.findByIsActiveTrueOrderByCreatedAtDesc());
        model.addAttribute("selectedBrandId", zone.get().getBrand().getBrandId());
        model.addAttribute("formAction", "/zones/edit/" + id);
        return "zone_form";
    }

    @PostMapping("/zones/edit/{id}")
    public String editZone(@PathVariable Integer id,
                           @RequestParam(required = false) String zoneName,
                           @RequestParam(required = false) Integer brandId,
                           Model model, RedirectAttributes redirectAttributes) {
        Optional<ZoneModel> zone = zoneRepository.findById(id);
        Optional<BrandModel> brand = brandId == null ? Optional.empty() : brandRepository.findById(brandId);
        String trimmedName = zoneName == null ? "" : zoneName.trim();
        String error = zone.isEmpty() || !Boolean.TRUE.equals(zone.get().getIsActive())
                ? "Active zone not found."
                : trimmedName.isEmpty() || trimmedName.length() > 50
                    ? "Zone name is required and cannot exceed 50 characters."
                    : brand.isEmpty() || !Boolean.TRUE.equals(brand.get().getIsActive())
                        ? "Select an active brand." : null;
        if (error != null) {
            if (zone.isEmpty() || !Boolean.TRUE.equals(zone.get().getIsActive())) {
                redirectAttributes.addFlashAttribute("error", error);
                return "redirect:/#zones";
            }
            model.addAttribute("error", error);
            model.addAttribute("zone", zone.get());
            model.addAttribute("zoneName", zoneName);
            model.addAttribute("brands", brandRepository.findByIsActiveTrueOrderByCreatedAtDesc());
            model.addAttribute("selectedBrandId", brandId);
            model.addAttribute("formAction", "/zones/edit/" + id);
            return "zone_form";
        }

        zone.get().setZoneName(trimmedName);
        zone.get().setBrand(brand.get());
        zoneRepository.save(zone.get());
        redirectAttributes.addFlashAttribute("success", "Zone updated successfully.");
        return "redirect:/#zones";
    }

    @PostMapping("/zones/delete/{id}")
    public String deleteZone(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<ZoneModel> zone = zoneRepository.findById(id);
        if (zone.isEmpty() || !Boolean.TRUE.equals(zone.get().getIsActive())) {
            redirectAttributes.addFlashAttribute("error", "Active zone not found.");
        } else {
            zone.get().setIsActive(false);
            zoneRepository.save(zone.get());
            redirectAttributes.addFlashAttribute("success", "Zone deactivated successfully.");
        }
        return "redirect:/#zones";
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
