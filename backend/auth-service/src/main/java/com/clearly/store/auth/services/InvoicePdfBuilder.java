package com.clearly.store.auth.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Map;

final class InvoicePdfBuilder {
    private static final Font SMALL = new Font(Font.HELVETICA, 8);
    private static final Font SMALL_BOLD = new Font(Font.HELVETICA, 8, Font.BOLD);
    private static final Font BODY = new Font(Font.HELVETICA, 9);
    private static final Font BODY_BOLD = new Font(Font.HELVETICA, 9, Font.BOLD);
    private InvoicePdfBuilder() {}

    static byte[] build(Map<String,Object> seller, Map<String,Object> buyer, String invoiceNo, Object date,
                        List<Map<String,Object>> items, BigDecimal subtotal, BigDecimal delivery, BigDecimal total) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 24, 24, 24, 24);
        PdfWriter.getInstance(document, out); document.open();

        PdfPTable header = new PdfPTable(new float[]{1.15f, 4.85f}); header.setWidthPercentage(100);
        PdfPCell logoCell = cell("", 10, Element.ALIGN_CENTER); logoCell.setRowspan(3); logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        URL logoUrl = InvoicePdfBuilder.class.getResource("/adhithya-logo.png");
        if (logoUrl != null) { Image logo = Image.getInstance(logoUrl); logo.scaleToFit(72, 43); logo.setAlignment(Image.ALIGN_CENTER); logoCell.addElement(logo); }
        header.addCell(logoCell);
        PdfPCell title = cell("TAX INVOICE", 10, Element.ALIGN_CENTER); title.setPhrase(new Phrase("TAX INVOICE", new Font(Font.HELVETICA, 11, Font.BOLD | Font.UNDERLINE))); header.addCell(title);
        PdfPCell name = cell(text(seller,"displayName","ADHITHYA CHEMICALS"), 10, Element.ALIGN_CENTER); name.setPhrase(new Phrase(text(seller,"displayName","ADHITHYA CHEMICALS"), new Font(Font.HELVETICA, 18, Font.BOLD))); header.addCell(name);
        String sellerLine = join(seller.get("addressLine1"), seller.get("addressLine2"), seller.get("city"), seller.get("state")+" - "+seller.get("postalCode"));
        header.addCell(cell(sellerLine+"\nTel.: "+text(seller,"phone","")+"  |  "+text(seller,"email",""), 8, Element.ALIGN_CENTER));
        document.add(header);

        PdfPTable details = new PdfPTable(new float[]{1,1}); details.setWidthPercentage(100);
        String party = "Party Details:\n"+text(buyer,"name","")+"\n"+join(buyer.get("address"),buyer.get("streetAddress"),buyer.get("city"),buyer.get("state")+" - "+buyer.get("postalCode"))+"\nGSTIN / UIN: "+text(buyer,"gstin","-")+"\nMobile: "+text(buyer,"phone","-");
        String invoice = "Invoice No.     : "+invoiceNo+"\nDated              : "+String.valueOf(date)+"\nPlace of Supply : "+text(buyer,"state","-")+"\nReverse Charge : N\nBill Type          : ONLINE\nPayment Status : PAID";
        details.addCell(cell(party, 9, Element.ALIGN_LEFT)); details.addCell(cell(invoice, 9, Element.ALIGN_LEFT)); document.add(details);

        PdfPTable goods = new PdfPTable(new float[]{.45f,3.4f,.75f,.75f,1.05f,1.15f}); goods.setWidthPercentage(100); goods.setHeaderRows(1);
        for(String heading:List.of("S.N.","Description of Goods","Qty.","Unit","Unit Price","Amount (Rs.)")) goods.addCell(head(heading));
        int serial=1; for(Map<String,Object> item:items){
            goods.addCell(body(String.valueOf(serial++),Element.ALIGN_CENTER)); goods.addCell(body(text(item,"productName",text(item,"description","Products supplied")),Element.ALIGN_LEFT));
            goods.addCell(body(text(item,"quantity",text(item,"items","1")),Element.ALIGN_RIGHT)); goods.addCell(body(text(item,"unit","Nos."),Element.ALIGN_CENTER));
            goods.addCell(body(money(item.getOrDefault("unitPrice",item.getOrDefault("amount",BigDecimal.ZERO))),Element.ALIGN_RIGHT)); goods.addCell(body(money(item.getOrDefault("lineTotal",item.getOrDefault("amount",BigDecimal.ZERO))),Element.ALIGN_RIGHT));
        }
        for(int i=0;i<Math.max(0,7-items.size());i++){goods.addCell(body("",Element.ALIGN_CENTER));for(int x=1;x<6;x++)goods.addCell(body("",Element.ALIGN_LEFT));}
        document.add(goods);

        PdfPTable totals = new PdfPTable(new float[]{4.85f,1.15f}); totals.setWidthPercentage(100);
        totals.addCell(right("Sub Total")); totals.addCell(right(money(subtotal)));
        totals.addCell(right("Delivery")); totals.addCell(right(money(delivery)));
        PdfPCell grandLabel=right("Grand Total"); grandLabel.setPhrase(new Phrase("Grand Total",BODY_BOLD)); totals.addCell(grandLabel);
        PdfPCell grand=right("Rs. "+money(total)); grand.setPhrase(new Phrase("Rs. "+money(total),BODY_BOLD)); totals.addCell(grand); document.add(totals);

        PdfPTable bank = new PdfPTable(1); bank.setWidthPercentage(100);
        bank.addCell(cell("Amount in words: "+numberWords(total)+" Only",9,Element.ALIGN_LEFT));
        bank.addCell(cell("Bank Details: Add bank account, IFSC and branch details in the seller profile.",8,Element.ALIGN_LEFT)); document.add(bank);
        PdfPTable footer = new PdfPTable(new float[]{1.05f,1.65f}); footer.setWidthPercentage(100);
        footer.addCell(cell("Terms & Conditions\n1. Goods once sold will not be taken back.\n2. Subject to Andhra Pradesh jurisdiction only.",8,Element.ALIGN_LEFT));
        PdfPCell sign=cell("Receiver's Signature:\n\n\nfor "+text(seller,"displayName","ADHITHYA CHEMICALS")+"\nAuthorised Signatory",8,Element.ALIGN_RIGHT); sign.setVerticalAlignment(Element.ALIGN_BOTTOM); footer.addCell(sign); document.add(footer);
        document.close(); return out.toByteArray();
    }

    private static PdfPCell cell(String value,int padding,int align){PdfPCell c=new PdfPCell(new Phrase(value,BODY));c.setPadding(padding);c.setHorizontalAlignment(align);c.setBorderColor(Color.BLACK);return c;}
    private static PdfPCell head(String value){PdfPCell c=cell(value,5,Element.ALIGN_CENTER);c.setPhrase(new Phrase(value,SMALL_BOLD));c.setBackgroundColor(new Color(246,246,246));return c;}
    private static PdfPCell body(String value,int align){PdfPCell c=cell(value,5,align);c.setPhrase(new Phrase(value,SMALL));c.setMinimumHeight(23);return c;}
    private static PdfPCell right(String value){return cell(value,5,Element.ALIGN_RIGHT);}
    private static String text(Map<String,Object> map,String key,String fallback){Object value=map.get(key);return value==null||String.valueOf(value).isBlank()?fallback:String.valueOf(value);}
    private static String join(Object... values){StringBuilder s=new StringBuilder();for(Object v:values){if(v==null)continue;String x=String.valueOf(v).trim();if(x.isBlank()||x.startsWith("null"))continue;if(s.length()>0)s.append(", ");s.append(x);}return s.toString();}
    private static String money(Object value){try{return new BigDecimal(String.valueOf(value)).setScale(2,java.math.RoundingMode.HALF_UP).toPlainString();}catch(Exception e){return "0.00";}}
    private static String numberWords(BigDecimal value){return "Rupees "+value.setScale(2,java.math.RoundingMode.HALF_UP).toPlainString();}
}
