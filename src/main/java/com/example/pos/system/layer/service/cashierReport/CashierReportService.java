package com.example.pos.system.layer.service.cashierReport;

import com.example.pos.system.constant.JavaConstant;
import com.example.pos.system.constant.JavaRoundUp;
import com.example.pos.system.domain.models.SummeryCashierReport;
import com.example.pos.system.domain.models.VatProductModel;
import com.example.pos.system.feature.employee.EmployeeRepository;
import com.example.pos.system.layer.projections.LastInvoiceProjection;
import com.example.pos.system.layer.projections.SaleSomeFieldProject;
import com.example.pos.system.layer.projections.discountProjection.DiscountProjection;
// import com.example.pos.connection1.repository.EmployeeRepository;
import com.example.pos.system.layer.projections.exchange_projection.ExchangeProjection;
import com.example.pos.system.layer.repository.SaleDetailsRepository;
import com.example.pos.system.layer.repository.SaleRepository;
import com.example.pos.system.layer.repository.UserRepository;
import com.example.pos.system.layer.repository.companyRepository.CompanyRepository;
import com.example.pos.system.layer.repository.paymentRepository.PaymentRepository;
import com.example.pos.system.layer.repository.shiftRepository.CloseShiftRepository;
import com.example.pos.system.layer.repository.shiftRepository.OpenShiftRepository;
import org.apache.commons.math3.dfp.DfpField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.pos.system.domain.general.CloseShift;
import com.example.pos.system.domain.settings.Company;
import com.example.pos.system.domain.settings.Employee;
import com.example.pos.system.domain.OpenShift;
import com.example.pos.system.domain.User;
import java.text.DecimalFormat;
import java.util.*;
import java.math.*;

@Service
public class CashierReportService {

    @Autowired
    private CompanyRepository repoCompany;

    @Autowired
    private EmployeeRepository repoEmp;

    @Autowired
    private UserRepository repoUser;

    @Autowired
    private OpenShiftRepository reposOpenShift;

    @Autowired
    private CloseShiftRepository closeShiftRepo;

    @Autowired
    private SaleRepository repoSale;

    @Autowired
    private PaymentRepository repoPay;

    @Autowired
    private SaleDetailsRepository repoSaleDetail;

    private HashMap<String, Object> map = new HashMap<>();
    private Double subTotal = 0.00;

    public Map<String, Object> cashierReport(String userCode, int userId, String posId) {

        int id = userId;
        // get company info
        Company company = repoCompany.getInfoCompany();
        map.put("companyName", company.getCompanyName());
        map.put("companyContact", company.getContact());
        map.put("companyAddress", company.getAddress());
        map.put("companyLogo", company.getPhoto());

        // get user name
        User user = repoUser.getUserById(id);
        Optional<Employee> employee = repoEmp.findByIdAndStatusTrueAndIsDeletedFalse(user.getEmpId());
        map.put("userName", employee.get().getNameEn());

        // get posId, openDate , openCash from openShift
        OpenShift openShift = reposOpenShift.getDataOpenShift(userCode, JavaConstant.currentDate, posId);

        map.put("posId", openShift.getPosId());
        map.put("openDate", openShift.getOpenTime());
        map.put("openCashKhr", openShift.getReserveKhr());
        map.put("openCashUsd", openShift.getReserveUsd());

        double _usd = openShift.getReserveKhr().doubleValue() / JavaConstant.exchangeRate;
        double totalWithdrawal = _usd + openShift.getReserveUsd().doubleValue();
        totalWithdrawal = JavaConstant.getTwoPrecision(totalWithdrawal);

        // get closeCash, closeDate from CloseShift
        CloseShift closeShift = closeShiftRepo.getCloseShift(userCode, JavaConstant.currentDate, posId);

        Double cashierCount = closeShift.getExpress().doubleValue() +
                closeShift.getKhqrMnk().doubleValue() +
                closeShift.getKhqrAba().doubleValue() +
                closeShift.getCreditCard().doubleValue() +
                closeShift.getCashUsd().doubleValue() +
                JavaConstant.getTwoPrecision(closeShift.getCashKhr().doubleValue() / JavaConstant.exchangeRate);


        List<ExchangeProjection> exchanges = repoSale.getExchange(userCode,posId);

        double sumExchangeUSD = 0;
        double sumExchangeKHR = 0;

        for( ExchangeProjection val : exchanges ) {
            sumExchangeUSD += val.getChange_usd().doubleValue();
            sumExchangeKHR += val.getChange_khr().doubleValue();
        }


        sumExchangeUSD = sumExchangeUSD + sumExchangeKHR/ JavaConstant.exchangeRate;


        sumExchangeUSD = JavaConstant.getTwoPrecision(sumExchangeUSD);

        cashierCount = cashierCount - sumExchangeUSD;

        map.put("closeCash", 1);
        map.put("cashierCount", BigDecimal.valueOf(Double.valueOf(JavaConstant.getTwoPrecision(cashierCount))));
        map.put("closeDate", closeShift.getCloseTime());

        // Sale summery
        SalesSummery(id, posId, userCode);
        vatProductSummery(posId, userCode);
        summerAllProVat(posId, userCode, totalWithdrawal);
        // // payment summery
        paymentSummery(id, posId, userCode);
        // // discount summery
        discountSummery(id, posId, userCode);
        return map;
    }

    public void summerAllProVat(String posId, String userCode, double totalWithdrawal) {
        ArrayList<VatProductModel> data = new ArrayList<>();

//        Double vat = repoSaleDetail.vat(JavaConstant.currentDate, posId, userCode);
//        vat = vat == null ? 0 : vat;
        map.put("totalWithdrawal", 0);
        map.put("cashierTotal",  0);

        Double noneVat = repoSaleDetail.noneVat(JavaConstant.currentDate, posId, userCode);
        noneVat = noneVat == null ? 0 : noneVat;

        Double vatStateChrge = repoSaleDetail.vatStateCharge(JavaConstant.currentDate, posId, userCode);
        vatStateChrge = vatStateChrge == null ? 0 : vatStateChrge;

        Double plt = repoSaleDetail.plt(JavaConstant.currentDate, posId, userCode);
        plt = plt == null ? 0 : plt;

        data.add(new VatProductModel("VAT Taxable Value", BigDecimal.valueOf(Double.valueOf(JavaConstant.getTwoPrecision(subTotal)))));
        data.add(new VatProductModel("Non-VAT Taxable Value", BigDecimal.valueOf(Double.valueOf(JavaConstant.getTwoPrecision(noneVat)))));
        data.add(new VatProductModel("VAT State Charge Value",
                BigDecimal.valueOf(Double.valueOf(JavaConstant.getTwoPrecision(vatStateChrge)))));
        data.add(new VatProductModel("Public Lighting Tax Base", BigDecimal.valueOf(Double.valueOf(JavaConstant.getTwoPrecision(plt)))));
        map.put("SummeryAllProVat", data);
    }

    public void vatProductSummery(String posId, String userCode) {
        ArrayList<VatProductModel> data = new ArrayList<>();

        // old code
//        Double vat10 = repoSaleDetail.vat10(JavaConstant.currentDate, posId, userCode);
//        vat10 = vat10 == null ? 0 : vat10;

        Double vat3 = repoSaleDetail.vat3(JavaConstant.currentDate, posId, userCode);

        vat3 = vat3 == null ? 0 : vat3;

        Double vat10 = (subTotal/1.1)*0.1;
        vat10 = JavaConstant.getTwoPrecision(vat10);

        data.add(new VatProductModel("VAT 10 %", BigDecimal.valueOf(vat10)));
        data.add(new VatProductModel("Public Lighting Tax", BigDecimal.valueOf(vat3)));
        map.put("SummeryVat", data);
    }

    DecimalFormat df = new DecimalFormat("#.##");
    DecimalFormat dfKh = new DecimalFormat("#");

    public void discountSummery(int userId, String posId, String userCode) {
        List<Integer> listDiscount = new ArrayList<>();
        listDiscount.add(10);
        // listDiscount.add(15);
        listDiscount.add(20);
        listDiscount.add(30);
        listDiscount.add(50);
        List<SummeryCashierReport> discount = new ArrayList<>();
        for (int i = 0; i < listDiscount.size(); i++) {
            int disQty = 0;
            List<Integer> disStr = repoSaleDetail.totalQty(JavaConstant.currentDate, posId,
                    Double.valueOf(listDiscount.get(i)));
            if (!disStr.isEmpty())
                disQty = disStr.size();

            double disAmount = 0;
            String disAmountStr = repoSaleDetail.totalAmount(userId, listDiscount.get(i), JavaConstant.currentDate,
                    posId);
            if (disAmountStr != null)
                disAmount = Double.valueOf(disAmountStr);

            if (disQty > 0) {
                discount.add(new SummeryCashierReport(listDiscount.get(i) + "%", disQty,
                        BigDecimal.valueOf(Double.valueOf(JavaConstant.getTwoPrecision(disAmount)))));
            }

        }

        int _disQtyDollar = 0;
        String _qtyDollar = repoSaleDetail.totalQtyDollar(userId, JavaConstant.currentDate, posId);
        if (_qtyDollar != null)
            _disQtyDollar = Integer.valueOf(_qtyDollar);

        double _saledDollar = 0;
        String _amSaledDollar = repoSaleDetail.totalSaledDollar(userId, JavaConstant.currentDate, posId);

        if (_amSaledDollar != null)
            _saledDollar = Double.valueOf(_amSaledDollar);

        HashMap<String, Object> discountDollar = new HashMap<>();
        discountDollar.put("qtySaledDollar", _disQtyDollar);
        discountDollar.put("amountSaledDollar", BigDecimal.valueOf(Double.valueOf(JavaConstant.getTwoPrecision(_saledDollar))));

        HashMap<String, Object> _map = new HashMap<>();
        _map.put("percentag", discount);
        _map.put("cash", discountDollar);
        map.put("discountSummery", _map);
    }

    public void paymentSummery(int userId, String posId, String userCode) {

        List<Integer> qtyUsd = repoSale.countSaledNumUsd(userId, JavaConstant.currentDate, posId);
        List<DiscountProjection> _cashUsd = repoSale.countSaledUsd(userId, JavaConstant.currentDate, posId);
        double _calculateCashUsd=0;
        for( DiscountProjection d : _cashUsd ) {
            Integer _qty = d.getQty() - d.getQty_returned();
            Double _price = d.getPrice() * _qty;
            if( d.getDiscount_type() != null )  {
                if( d.getDiscount_type().equals("dollar") ) {
                    _calculateCashUsd += _price  - d.getDiscount()*_qty; 
                } else {
                    Double _val = _price  - (_price *  d.getDiscount())/100;
                    _calculateCashUsd += _val; 
                }
            } else {
                Double _val = _price  - (_price *  d.getDiscount())/100;
                _calculateCashUsd += _val;
            }
        }


        List<Integer> qtyKhr = repoSale.countSaledNumKhr(userId, JavaConstant.currentDate, posId);
        List<DiscountProjection> _cashKhr = repoSale.countSaledCashKhr(userId, JavaConstant.currentDate, posId);
        
        double _calculateCashKhr=0;
        for( DiscountProjection d : _cashKhr ) {
            Integer _qty = d.getQty() - d.getQty_returned();
            Double _price = d.getPrice() * _qty;

            if( d.getDiscount_type() != null )  {
                if( d.getDiscount_type().equals("dollar") ) {
                    _calculateCashKhr += _price - d.getDiscount()*_qty; 
                } else {
                    Double _val = _price  - (_price *  d.getDiscount())/100;
                    _calculateCashKhr += _val; 
                }
            } else {
                Double _val = _price  - (_price *  d.getDiscount())/100;
                _calculateCashKhr += _val;
            }
        }



        int qtyAba = repoSale.countSaledNumAba(userId, JavaConstant.currentDate, posId);
        Double _cashAba = repoSale.countSaledAba(userId, JavaConstant.currentDate, posId);
        _cashAba = _cashAba == null ? 0 : _cashAba;

        int qtyMnk = repoSale.countSaledNumMnk(userId, JavaConstant.currentDate, posId);
        Double _cashMnk = repoSale.countSaledMnk(userId, JavaConstant.currentDate, posId);
        _cashMnk = _cashMnk == null ? 0 : _cashMnk;

        int qtyExpress = 0;
        String qtyExpressStr = repoSale.totalCountQtyExpress(userId, JavaConstant.currentDate, posId,
                JavaConstant.currentDate, userCode);
        if (qtyExpressStr != null)
            qtyExpress = Integer.valueOf(qtyExpressStr);

        int qtyCredit = repoSale.countSaledNumCredit(userId, JavaConstant.currentDate, posId);
        Double _cashCredit = repoSale.countSaledCredit(userId, JavaConstant.currentDate, posId);
        _cashCredit = _cashCredit == null ? 0 : _cashCredit;

        ArrayList<SummeryCashierReport> payment = new ArrayList<>();



        String _cash_riel = JavaRoundUp.setRoundNumber(_calculateCashKhr * JavaConstant.exchangeRate);

        Double _convert_cash_riel_to_usd = Double.valueOf(_cash_riel.replace(",",""));

        payment.add(new SummeryCashierReport(
                "Cash-Riels " +  _cash_riel, qtyKhr.size(),
                BigDecimal.valueOf(Double.valueOf(JavaConstant.getTwoPrecision(_convert_cash_riel_to_usd/JavaConstant.exchangeRate)))));


        payment.add(new SummeryCashierReport("Cash- Dollars", qtyUsd.size(),
                BigDecimal.valueOf(Double.valueOf(JavaConstant.getTwoPrecision(_calculateCashUsd)))));
        payment.add(new SummeryCashierReport("MNK QR Pay", qtyMnk,
                BigDecimal.valueOf(Double.valueOf(JavaConstant.getTwoPrecision(_cashMnk)))));
        payment.add(new SummeryCashierReport("ABA QR Pay", qtyAba,
                BigDecimal.valueOf(Double.valueOf(JavaConstant.getTwoPrecision(_cashAba)))));
        payment.add(new SummeryCashierReport("ABA-Card Payment", qtyCredit,
                BigDecimal.valueOf(Double.valueOf(JavaConstant.getTwoPrecision(_cashCredit)))));

        map.put("summeryPayemnt", payment);
    }

    public void SalesSummery(int userId, String posId, String userCode) {
        String paymentNoFirst = repoPay.getFirstPaymentNumber(userId, JavaConstant.currentDate);
        List<LastInvoiceProjection> paymentNoLast = repoPay.getLastPaymentNumber(userId, JavaConstant.currentDate);

        String _lastPay="";
        for(  LastInvoiceProjection l : paymentNoLast ) {
            if( l.getIs_return() == null ) {
                _lastPay = l.getPayment_no();
            } else {
                _lastPay = l.getReturn_number();
            }
         }


        map.put("paymentNoFirst", paymentNoFirst);
        map.put("paymentNoLast", _lastPay);

        List<Integer> qtyDiscount = repoSaleDetail.totalQtyDiscount(userId , JavaConstant.currentDate , posId);

        double amountDiscounts = 0.00;
        List<DiscountProjection>  listDiscountQty = repoSaleDetail.totalAmountDiscount(userId , JavaConstant.currentDate , posId);
        for( DiscountProjection d : listDiscountQty ) {
                if( d.getDiscount_type() != null ) {
                    if( d.getDiscount_type().equals("dollar") ) {
                        amountDiscounts+= d.getDiscount()*d.getQty();
                    } else {
                        double _val = ( d.getPrice() * d.getQty() * d.getDiscount()) / 100;
                        amountDiscounts += _val;
                    }
                } else {
                    double _val = ( d.getPrice() * d.getQty() * d.getDiscount()) / 100;
                    amountDiscounts += _val;
                }
        }

        List<Integer> returnQty = repoSaleDetail.numRetured(userId , JavaConstant.currentDate , posId);

        // double returnAmount = 0;
        Double returnAmountDiscount = repoSaleDetail.totalReturnAmountDiscount(
                JavaConstant.currentDate,
                posId,
                userCode);


        returnAmountDiscount = returnAmountDiscount == null ? 0 : returnAmountDiscount;

        int numOfSale = repoSaleDetail.numOfSale(JavaConstant.currentDate, posId, userCode);
        List<SaleSomeFieldProject> totalAmount = repoSaleDetail.totalSaledAmount(JavaConstant.currentDate, posId,userCode);

        double _sumTotal = 0;
        for (SaleSomeFieldProject s : totalAmount) {
            if (s.getDiscount_type() != null) {
                if (s.getDiscount_type().equals("promotion")) { // this case means items have discount from backend
                    _sumTotal += s.getAmount();
                } else {
                    _sumTotal += s.getResults();
                }
            } else {
                _sumTotal += s.getResults();
            }

        }

        ArrayList<SummeryCashierReport> summery = new ArrayList<>();

        returnAmountDiscount = JavaConstant.getTwoPrecision(returnAmountDiscount);

        summery.add(new SummeryCashierReport("Total Sales", numOfSale,BigDecimal.valueOf(JavaConstant.getTwoPrecision(_sumTotal))));

        summery.add(new SummeryCashierReport("Total Refund/Return", returnQty.size(), BigDecimal.valueOf(returnAmountDiscount)));

        summery.add(new SummeryCashierReport("Total Voids", 0, BigDecimal.valueOf(0)));

        summery.add(new SummeryCashierReport("Discounts", qtyDiscount.size(),BigDecimal.valueOf(JavaConstant.getTwoPrecision(amountDiscounts))));


        // calculate subTotal
        subTotal =  JavaConstant.getTwoPrecision(_sumTotal) - returnAmountDiscount - JavaConstant.getTwoPrecision(amountDiscounts);


        map.put("SummerySale", summery);
    }

    public static String addCommas(String str) {
        StringBuilder result = new StringBuilder();
        int length = str.length();
        int count = 0;

        // Iterate through the string from right to left
        for (int i = length - 1; i >= 0; i--) {
            char c = str.charAt(i);
            result.insert(0, c); // Insert character at the beginning of the result string
            count++;

            // Insert comma after every 3 characters, except at the beginning
            if (count % 3 == 0 && i != 0) {
                result.insert(0, ',');
            }
        }

        return result.toString();
    }
}
