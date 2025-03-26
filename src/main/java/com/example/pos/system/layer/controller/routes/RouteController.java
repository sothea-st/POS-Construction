package com.example.pos.system.layer.controller.routes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import com.example.pos.system.layer.service.shiftService.CloseShiftService;
import com.example.pos.system.layer.service.shiftService.OpenShiftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.pos.system.layer.DTO.ReportRequest;
import com.example.pos.system.layer.DTO.categoryDto.CategoryRequest;
import com.example.pos.system.layer.DTO.categoryDto.CategoryResponse;
import com.example.pos.system.constant.JavaResponse;
import com.example.pos.system.constant.JavaConstant;
import com.example.pos.system.constant.JavaValidation;
import com.example.pos.system.domain.settings.Category;
import com.example.pos.system.domain.general.CloseShift;
import com.example.pos.system.domain.settings.Company;
import com.example.pos.system.domain.stock.Import;
import com.example.pos.system.domain.OpenShift;
import com.example.pos.system.domain.Sale;
import com.example.pos.system.domain.settings.Supplier;
import com.example.pos.system.domain.models.ProductAddRemoveQty;
import com.example.pos.system.domain.sourceData.CancelItem;
import com.example.pos.system.domain.sourceData.CurrencyValue;
import com.example.pos.system.domain.sourceData.CustomerType;
import com.example.pos.system.domain.sourceData.Reason;
import com.example.pos.system.domain.sourceData.ReturnProduct;
import com.example.pos.system.domain.sourceData.Source;
import com.example.pos.system.layer.projections.ReportImport.ReportImportProjection;
import com.example.pos.system.layer.repository.SaleRepository;
import com.example.pos.system.layer.repository.productProjection.ProductProjection;
import com.example.pos.system.layer.repository.shiftRepository.CloseShiftRepository;
import com.example.pos.system.layer.repository.shiftRepository.OpenShiftRepository;
import com.example.pos.system.layer.service.CategoryService;
// import com.example.pos.connection1.service.EmployeeService;
import com.example.pos.system.layer.service.ImportService;
import com.example.pos.system.layer.service.SaleService;
import com.example.pos.system.layer.service.SupplierService;
import com.example.pos.system.layer.service.cashierReport.CashierReportService;
import com.example.pos.system.layer.service.companyService.CompanyService;
import com.example.pos.system.layer.service.paymentService.ReprintService;
import com.example.pos.system.layer.service.sourceDataService.CancelItemService;
import com.example.pos.system.layer.service.sourceDataService.CurrencyValueService;
import com.example.pos.system.layer.service.sourceDataService.CustomerTypeService;
import com.example.pos.system.layer.service.sourceDataService.ReasonService;
import com.example.pos.system.layer.service.sourceDataService.ReturnProductService;
import com.example.pos.system.layer.service.sourceDataService.SourceService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.time.*;

@RestController
public class RouteController {

     @RequestMapping("/api/category")
     @RestController
     public static class RouteCategory {
          @Autowired
          private CategoryService service;

//          @GetMapping("/code/{code}")
//          public ResponseEntity<?> getCategoryByCode(@Valid @PathVariable("code") String code) {
//               List<CategoryResponse> data = service.getCategoryByCode(code);
//               return JavaResponse.success(data);
//          }

          @PostMapping
          public ResponseEntity<?> saveCategory(@Valid @RequestBody CategoryRequest c) {
               Category data = service.saveCategory(c);
               return JavaResponse.success(data);
          }

          @GetMapping("/parentId/{parentId}")
          public ResponseEntity<?> getCategory(@Valid @PathVariable("parentId") int parentId) {
               List<CategoryResponse> data = service.getCategory(parentId);
               return JavaResponse.success(data);
          }

          @PutMapping("/{id}")
          public ResponseEntity<?> updateCategory(@PathVariable("id") int id, @Valid @RequestBody Category category) {
               Category data = service.updateCategory(id, category);
               return JavaResponse.success(data);
          }

          @GetMapping("/{id}")
          public ResponseEntity<?> getCategoryById(@PathVariable("id") int id) {
               CategoryResponse data = service.getCategoryById(id);
               return JavaResponse.success(data);
          }

          @DeleteMapping("/{id}")
          public ResponseEntity<?> deleteCategory(@PathVariable("id") int id) {
               service.deleteCategory(id);
               return JavaResponse.deleteSuccess(id);
          }

//          @GetMapping("/code/{code}/search/{catNameEn}")
//          public ResponseEntity<?> search(@Valid @PathVariable("code") String code,
//                    @PathVariable("catNameEn") String searchValue) {
//               List<CategoryResponse> data = service.search(code, searchValue);
//               return JavaResponse.success(data);
//          }
     }

     @RequestMapping("/api/supplier")
     @RestController
     public static class RouteSupplier {
          @Autowired
          private SupplierService service;

          @PostMapping()
          public ResponseEntity<?> addSupplier(@Valid @ModelAttribute Supplier supplier) {
               HashMap<String, String> err = new HashMap<>();
               String key = "contact";
               String contact = JavaValidation.checkPhone(supplier.getContact());

               if (!contact.isEmpty()) {
                    err.put(key, contact);
                    return ResponseEntity.status(500).body(err);
               }

               Supplier s = service.addSupplier(supplier);
               return JavaResponse.success(s);
          }

          @GetMapping()
          public ResponseEntity<?> getSupplier() {
               ArrayList<Supplier> data = service.getSupplier();
               return JavaResponse.success(data);
          }

          @GetMapping("/{id}")
          public ResponseEntity<?> getSupplierById(@PathVariable("id") int id) {
               Supplier data = service.getSupplierById(id);
               return JavaResponse.success(data);
          }

          @PutMapping("/{id}")
          public ResponseEntity<?> updateSupplier(@PathVariable("id") int id, @Valid @RequestBody Supplier s) {
               HashMap<String, String> err = new HashMap<>();
               String key = "contact";
               String contact = JavaValidation.checkPhone(s.getContact());

               if (!contact.isEmpty()) {
                    err.put(key, contact);
                    return ResponseEntity.status(500).body(err);
               }
               Supplier data = service.updateSupplier(id, s);
               return JavaResponse.success(data);
          }

          @DeleteMapping("/{id}")
          public ResponseEntity<?> deleteSupplier(@PathVariable("id") int id, @RequestBody Supplier s) {
               service.deleteSupplier(id, s);
               return JavaResponse.success("delete success");
          }
     }

     // @RequestMapping("/api/employee")
     // @RestController
     // public static class RouteEmployee {
     //      @Autowired
     //      private EmployeeService service;

     //      @GetMapping("/userAccount")
     //      public ResponseEntity<?> getUserAccount() {
     //           return JavaResponse.success(service.getUserAccount());
     //      }

     //      @GetMapping("/searchEmployee/{nameEn}")
     //      public ResponseEntity<?> searchEmp(@PathVariable("nameEn") String nameEn) {
     //           return JavaResponse.success(service.searchEmp(nameEn));
     //      }

     //      @GetMapping("/searchUserAccount/{value}")
     //      public ResponseEntity<?> searchUserAccount(@PathVariable("value") String value) {
     //           return JavaResponse.success(service.seachUserAccount(value));
     //      }

     //      @PostMapping
     //      public ResponseEntity<?> addEmployee(@Valid @ModelAttribute Employee e,
     //                @RequestParam(value = "image", required = false) MultipartFile file) throws IOException {
     //           HashMap<String, String> err = new HashMap<>();
     //           String key = "contact";
     //           String contact = JavaValidation.checkPhone(e.getContact());

     //           if (!contact.isEmpty()) {
     //                err.put(key, contact);
     //                return ResponseEntity.status(500).body(err);
     //           }
     //           Employee data = service.addEmployee(e, file);
     //           return JavaResponse.success(data);
     //      }

     //      @GetMapping
     //      public ResponseEntity<?> getEmployee() {
     //           List<Employee> data = service.getEmployee();
     //           return JavaResponse.success(data);
     //      }

     //      @GetMapping("/{id}")
     //      public ResponseEntity<?> getEmployeeById(@PathVariable("id") int id) {
     //           Employee data = service.getEmployeeById(id);
     //           return JavaResponse.success(data);
     //      }

     //      @DeleteMapping("/{id}")
     //      public ResponseEntity<?> deleteEmployeeById(@PathVariable("id") int id) {
     //           service.deleteEmployeeById(id);
     //           return JavaResponse.deleteSuccess(id);
     //      }

     //      @PostMapping("/{id}")
     //      public ResponseEntity<?> updateEmployee(@Valid @PathVariable("id") int id, @ModelAttribute Employee e,
     //                @RequestParam(name = "image", required = false) MultipartFile file) throws IOException {

     //           HashMap<String, String> err = new HashMap<>();
     //           String key = "contact";
     //           String contact = JavaValidation.checkPhone(e.getContact());

     //           if (!contact.isEmpty()) {
     //                err.put(key, contact);
     //                return ResponseEntity.status(500).body(err);
     //           }
     //           Employee data = service.updateEmployee(id, e, file);
     //           return JavaResponse.success(data);
     //      }

     //      @GetMapping("/readFileById/{id}")
     //      public ResponseEntity<byte[]> getImage(@PathVariable("id") String id) throws IOException {
     //           byte[] data = service.getImageEmployee(id);
     //           return ResponseEntity.status(HttpStatus.OK)
     //                     .contentType(MediaType.valueOf(IMAGE_PNG_VALUE))
     //                     .body(data);
     //      }

     // }

     @RestController
     @RequestMapping("/api/imports")
     public static class RouteImport {
          @Autowired
          private ImportService service;

          @PostMapping
          public ResponseEntity<?> addImport(@Valid @RequestBody Import i) {
               service.addImport(i);
               return JavaResponse.success("success insert");
          }

          @PostMapping("/updateQty")
          public ResponseEntity<?> updateQty(@RequestBody ProductAddRemoveQty p) {
               int _qty = service.updateQty(p);
               return ResponseEntity.ok().body(Map.of("qtyUpdate", _qty, "msg", "success"));
          }

          @PostMapping("/reportImport")
          public ResponseEntity<?> reportImport(@Valid @RequestBody ReportRequest reportRequest) {
               List<ReportImportProjection> data = service.reportImport(reportRequest);
               return JavaResponse.success(data);
          }

     }

     @RestController
     @RequestMapping("/api/sale")
     @RequiredArgsConstructor
     public static class RouteSale {
          private final SaleService service;
          private final HttpSession session;
          private final OpenShiftRepository repoOpen;
          private final SaleRepository saleRepository;

          @PostMapping
          public ResponseEntity<?> saleProduct(@Valid @RequestBody Sale s) throws Exception {
               var userCode = session.getAttribute(JavaConstant.userCode);

               OpenShift countOpenShift = repoOpen.countOpenShift(s.getUserCode(), JavaConstant.currentDate);
               HashMap<String, Object> map = new HashMap<>();
               // protect when user try to processing sale but user does not open shift first
               if (countOpenShift == null || countOpenShift.getNumberOpenShift() == 0) {
                    map.put(JavaConstant.message, JavaConstant.openShift);
                    return JavaResponse.error(map);
               }

               var data = service.saleProduct(s);
               return JavaResponse.success(data);
          }

          @GetMapping("/reportSaled")
          public ResponseEntity<?> reportSaled(
                    @RequestParam(name = "pageNumber", required = false) Integer pageNumber,
                    @RequestParam(name = "pageSize", required = false) Integer pageSize,
                    @RequestParam(name = "dateFrom") String dateFrom,
                    @RequestParam(name = "dateTo") String dateTo,
                    @RequestParam(name = "userId", required = false) Integer userId) {
               int count = saleRepository.countSalesData(LocalDate.parse(dateFrom), LocalDate.parse(dateTo), userId);
               return ResponseEntity.ok().body(Map.of("data",
                         service.reportSaled(dateFrom, dateTo, pageNumber, pageSize, userId), "count", count));
          }

          @GetMapping("/search/{search}")
          public  ResponseEntity<?> searchReport(
                  @RequestParam(name = "pageNumber", required = false) Integer pageNumber,
                  @RequestParam(name = "pageSize", required = false) Integer pageSize,
                  @RequestParam(name = "dateFrom") String dateFrom,
                  @RequestParam(name = "dateTo") String dateTo,
                  @RequestParam(name = "userId", required = false) Integer userId,
                  @PathVariable(name = "search") String search
          ){
               return ResponseEntity.ok().body(Map.of("data",
                       service.searchReportSaled(
                               dateFrom, dateTo, pageNumber, pageSize, userId ,search), "count", service.searchReportSaled(
                               dateFrom, dateTo, pageNumber, pageSize, userId ,search).size()));
          }
     }

     @RestController
     @RequestMapping("/api/reprint")
     public static class RouteReprint {
          @Autowired
          private ReprintService service;

          @GetMapping("/{paymentNo}")
          public ResponseEntity<?> getData(@PathVariable("paymentNo") String paymentNo) {
               var data = service.readData(paymentNo);
               return JavaResponse.success(data);
          }

          @GetMapping
          public ResponseEntity<?> getData() {
               var data = service.readData("");
               return JavaResponse.success(data);
          }
     }

     @RestController
     @RequestMapping("/api/source")
     public static class RouteSource {
          @Autowired
          private SourceService service;

          @PostMapping
          public ResponseEntity<?> addSource(@Valid @ModelAttribute Source s) {
               Source data = service.addSource(s);
               return JavaResponse.success(data);
          }

          @GetMapping
          public ResponseEntity<?> getSource() {
               List<Source> data = service.getSource();
               return JavaResponse.success(data);
          }

          @GetMapping("/{id}")
          public ResponseEntity<?> getSourceById(@PathVariable("id") int id) {
               Source data = service.getSourceById(id);
               return JavaResponse.success(data);
          }

          @DeleteMapping("/{id}")
          public ResponseEntity<?> deleteSource(@PathVariable("id") int id, @RequestBody Source s) {
               service.deleteSource(id, s);
               return JavaResponse.deleteSuccess(id);
          }

          @PutMapping("/{id}")
          public ResponseEntity<?> updateSource(@PathVariable("id") int id, @RequestBody Source s) {
               Source data = service.updateSource(id, s);
               return JavaResponse.success(data);
          }
     }

     @RestController
     @RequestMapping("/api/customerType")
     public static class RouteCustomerType {
          @Autowired
          private CustomerTypeService service;

          @PostMapping
          public ResponseEntity<?> add(@Valid @ModelAttribute CustomerType c) {
               CustomerType data = service.add(c);
               return JavaResponse.success(data);
          }

          @GetMapping
          public ResponseEntity<?> read() {
               List<CustomerType> data = service.read();
               return JavaResponse.success(data);
          }

          @GetMapping("/{id}")
          public ResponseEntity<?> readById(@PathVariable("id") int id) {
               CustomerType data = service.readById(id);
               return JavaResponse.success(data);
          }

          @DeleteMapping("/{id}")
          public ResponseEntity<?> delete(@PathVariable("id") int id, @RequestBody CustomerType c) {
               service.delete(id, c);
               return JavaResponse.deleteSuccess(id);
          }

          @PutMapping("/{id}")
          public ResponseEntity<?> update(@PathVariable("id") int id, @RequestBody CustomerType c) {
               CustomerType data = service.update(id, c);
               return JavaResponse.success(data);
          }
     }

     @RestController
     @RequestMapping("/api/company")
     public static class RouteCompany {
          @Autowired
          private CompanyService service;

          @PostMapping
          public ResponseEntity<?> addCompany(@Valid @ModelAttribute Company c,
                    @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
               Company data = service.addCompany(c, file);
               return JavaResponse.success(data);
          }

          @GetMapping
          public ResponseEntity<?> getCompnay() {
               List<Company> data = service.getCompany();
               return JavaResponse.success(data);
          }

          @GetMapping("/{id}")
          public ResponseEntity<?> getCompanyById(@PathVariable("id") int id) {
               Company data = service.getCompanyById(id);
               return JavaResponse.success(data);
          }

          @DeleteMapping("/{id}")
          public ResponseEntity<?> deleteCompany(@PathVariable("id") int id, @RequestBody Company c) {
               service.deleteCompany(id, c);
               return JavaResponse.deleteSuccess(id);
          }

          @PostMapping("/{id}")
          public ResponseEntity<?> updateCompany(@PathVariable("id") int id, @ModelAttribute Company c,
                    @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
               Company data = service.updateCompany(id, c, file);
               return JavaResponse.success(data);
          }
     }

     @RestController
     @RequestMapping("/api/cancelItem")
     public static class RouteCancel {
          @Autowired
          private CancelItemService service;

          @PostMapping("/{type}")
          public ResponseEntity<?> cancelItem(@PathVariable("type") String type, @RequestBody CancelItem c) {
               service.cancelAndDeleteItem(c, type);
               return JavaResponse.success("succes delelte item");
          }
     }

     @RestController
     @RequestMapping("/api/openShiftTime")
     public static class RouteOpenShift {
          @Autowired
          private OpenShiftService service;

          @PostMapping
          public ResponseEntity<?> openShift(@Valid @RequestBody OpenShift o) {
               OpenShift data = service.openShift(o);
               return JavaResponse.success(data);
          }

          @GetMapping("/{userCode}")
          public ResponseEntity<?> getOpenShift(@PathVariable("userCode") String userCode) {
               OpenShift data = service.getOpenShift(userCode);
               return JavaResponse.success(data);
          }
     }

     @RestController
     @RequestMapping("/api/closeShiftTime")
     public static class RouteCloseShift {
          @Autowired
          private CloseShiftService service;

          @Autowired
          private HttpSession session;

          @Autowired
          private CloseShiftRepository repoClose;

          @Autowired
          private OpenShiftRepository repoOpen;

          @PostMapping
          public ResponseEntity<?> closeShift(@RequestBody CloseShift c) {
               HashMap<String, Object> map = new HashMap<>();

               OpenShift countOpenShift = repoOpen.countOpenShift(c.getUserCode(), JavaConstant.currentDate);
               // protect when user try to processing sale but user does not open shift first
               if (countOpenShift == null || countOpenShift.getNumberOpenShift() == 0) {
                    map.put(JavaConstant.message, JavaConstant.closeOpenShfitFirst);
                    return JavaResponse.error(map);
               }

               CloseShift data = service.closeShift(c);
               return JavaResponse.success(data);
          }
     }

     @RestController
     @RequestMapping("/api/cashierReport")
     public static class RouteCashierRepot {
          @Autowired
          private CashierReportService service;

          @Autowired
          private CloseShiftRepository repoClose;

          @GetMapping
          public ResponseEntity<?> getCashierReport(@RequestParam("userCode") String userCode,
                    @RequestParam("userId") int userId, @RequestParam("posId") String posId) {
               HashMap<String, Object> map = new HashMap<>();
               CloseShift closeShift = repoClose.getCloseShift(userCode, JavaConstant.currentDate, posId);
               // protect when user try to processing sale but user does not open shift first
               if (closeShift == null) {
                    map.put(JavaConstant.message, JavaConstant.msgCloseShift);
                    return JavaResponse.error(map);
               }
               Map<String, Object> res = service.cashierReport(userCode, userId, posId);
               if (res.get("paymentNoFirst") == null) {
                    return ResponseEntity.ok().body(Map.of("msg", "NO_RESULT"));
               }
               return JavaResponse.success(res);
          }
     }

     @RestController
     @RequestMapping("/api/reason")
     public static class RouteReason {
          @Autowired
          private ReasonService service;

          @PostMapping
          public ResponseEntity<?> addReason(@Valid @RequestBody Reason reason) {

               Reason data = service.addReason(reason);
               return JavaResponse.success(data);
          }

          @GetMapping
          public ResponseEntity<?> getReason() {
               List<Reason> data = service.getReason();
               return JavaResponse.success(data);
          }

          @GetMapping("/{id}")
          public ResponseEntity<?> getReasonById(@PathVariable("id") int id) {
               Reason data = service.getReasonById(id);
               return JavaResponse.success(data);
          }

          @DeleteMapping("/{id}")
          public ResponseEntity<?> delete(@PathVariable("id") int id, @RequestBody Reason r) {
               service.delete(id, r);
               return JavaResponse.deleteSuccess(id);
          }

          @PutMapping("/{id}")
          public ResponseEntity<?> update(@PathVariable("id") int id, @RequestBody Reason r) {
               Reason data = service.update(id, r);
               return JavaResponse.success(data);
          }

          @GetMapping("/getReasonByCode/{code}")
          public ResponseEntity<?> getReasonByCode(@PathVariable("code") String code) {
               List<Reason> data = service.getReasonByCode(code);
               return JavaResponse.success(data);
          }
     }

     @RestController
     @RequestMapping("/api/returnProduct")
     public static class RouteReturnProduct {
          @Autowired
          private ReturnProductService service;

          @PostMapping
          public ResponseEntity<?> returnProduct(@Valid @RequestBody ReturnProduct r) {
               Map<String, Object> map = service.returnProduct(r);
               return JavaResponse.success(map);
          }

          @GetMapping("/{barcode}")
          public ResponseEntity<?> getProductByBarcode(@PathVariable("barcode") String barcode) {
               ProductProjection data = service.searchProdcutByBarcode(barcode);
               return JavaResponse.success(data);
          }
     }

     @RestController
     @RequestMapping("/api/currencyValue")
     public static class RouteCurrenValue {
          @Autowired
          private CurrencyValueService service;

          @PostMapping
          public ResponseEntity<?> addCurrency(@Valid @RequestBody CurrencyValue c) {
               CurrencyValue data = service.addCurrency(c);
               return JavaResponse.success(data);
          }

          @GetMapping
          public ResponseEntity<?> getAllCurrency() {
               List<CurrencyValue> data = service.getAllCurrencyValue();
               return JavaResponse.success(data);
          }

          @GetMapping("/{id}")
          public ResponseEntity<?> getCurrencyById(@PathVariable("id") int id) {
               CurrencyValue data = service.getCurrencyById(id);
               return JavaResponse.success(data);
          }

          @DeleteMapping("/{id}")
          public ResponseEntity<?> deleteCurrency(@PathVariable("id") int id, @RequestBody CurrencyValue c) {
               service.deleteCurrency(id, c);
               return JavaResponse.deleteSuccess(id);
          }

          @PutMapping("/{id}")
          public ResponseEntity<?> updateCurrency(@PathVariable("id") int id, @RequestBody CurrencyValue c) {
               CurrencyValue data = service.updateCurrency(id, c);
               return JavaResponse.success(data);
          }
     }

}
