import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.PrintStream;

public class AccountsReceivable {

    public static void main(String[] args) throws Exception {
        File masterRecords = new File("master_records.txt");
        File transactionRecords = new File("transaction_records.txt");
        File discounts = new File("discounts.txt");

        List<MasterRecord> masterList = new ArrayList<>();
        List<TransactionRecord> txList = new ArrayList<>();

        // Collect a list of all accounts for future reference
        List<Integer> accountList = new ArrayList<>();
        Scanner accountScanner = new Scanner(masterRecords);
        while (accountScanner.hasNext()) {
            accountList.add(accountScanner.nextInt());
            accountScanner.next();
            accountScanner.nextDouble();
        }
        accountScanner.reset();

        System.out.println(accountList);

        Scanner masterScan = new Scanner(masterRecords);

        Scanner discountScan = new Scanner(discounts);
        Scanner txScan = new Scanner(transactionRecords);
        List<String> txLines = new ArrayList<>();

        while (txScan.hasNextLine()) {
            txLines.add(txScan.nextLine());
        }
        System.out.println(txLines);
        txScan.close();

        int lastLine = 0;

        while (masterScan.hasNext()) {
            int account = masterScan.nextInt();
            String name = masterScan.next();
            double balance = masterScan.nextDouble();
            // int txAccountCopy = 0;

            MasterRecord newMasterRecord = new MasterRecord(account, name, balance);

            if (!masterList.contains(newMasterRecord)) {

                masterList.add(newMasterRecord);
                System.out.println(newMasterRecord);

                for (int i = lastLine; i < txLines.size(); i++) {
                    String line = txLines.get(i);
                    // System.out.println("\n______ "+line+"________\n");

                    Scanner lineScan = new Scanner(line);

                    String txType = lineScan.next();
                    int txAccount = lineScan.nextInt();

                    if (txAccount == account) {

                        // txAccountCopy = txAccount;

                        int txNum = lineScan.nextInt();

                        if (txType.equals("P")) {
                            // update master balance
                            // apply multiplier
                            double payment = lineScan.nextDouble();
                            txList.add(new PaymentRecord(txAccount, txNum,
                                    payment * (discountScan.nextDouble() / 100) + payment));
                        } else {
                            // order record
                            String item = lineScan.next();
                            int quantity = lineScan.nextInt();
                            double cost = lineScan.nextDouble();
                            txList.add(new OrderRecord(account, txNum, item, quantity, cost));
                        }

                    } else if (accountList.contains(txAccount)){

                        System.out.println("reached the end of data block");
                        System.out.println("i: " + i + "\nlastLine: " + lastLine);

                        System.out.println("Printing results");
                        for (int j = lastLine; j < i; j++) {
                            System.out.println(txList.get(j).getBalanceAdjustment());
                        }

                        lastLine = i;
                        break;
                    } else {
                        System.out.println("This transaction's account number doesn't exist in the master list.");
                        System.out.println("before removeing:");
                        System.out.println(txLines.size());
                        txLines.remove(i);
                        lastLine = i;
                        System.out.println("after removeing:");
                        System.out.println(txLines.size());
                        break;
                        }

                }

            } else {
                System.out.println("This master record already exists! Skipping current line...");
                masterScan.nextLine();
            }
        }


        // generate invoices
        File invoiceFile = new File("invoices.txt");
        PrintStream invoicePrint = new PrintStream(invoiceFile);
        
        for(MasterRecord record : masterList){
            invoicePrint.printf("%s\n\n","-".repeat(50));
            String customerName = record.getName();
            invoicePrint.println(customerName+"  (Customer #"+record.getAccount()+")\n");
            invoicePrint.printf("                  %-20s $%.2f\n\n","Previous Balance",record.getBalance());

            for(TransactionRecord tx : txList){

                if(record.getAccount() == tx.getAccount()){
                    invoicePrint.printf("Transaction #%02d",tx.getTxNumber());
                    double adjustment = tx.getBalanceAdjustment();

                    if (tx instanceof OrderRecord){
                        OrderRecord order = (OrderRecord) tx;
                        
                        invoicePrint.printf("   %-20s $%.2f\n", order.getItemName(), adjustment);
                        
                    }
                    else{
                        invoicePrint.printf("   %-20s-$%.2f\n","Payment", adjustment*-1 );
                    }

                    record.adjustBalance(adjustment);
                }

            }

            
            invoicePrint.printf("\n                  %-20s $%.2f\n\n\n","Balance Due", record.getBalance());
        }

    }
}

class MasterRecord {
    private int account;
    private String name;
    private double balance;

    public MasterRecord(int account, String name, double balance) {
        this.account = account;
        this.name = name;
        this.balance = balance;
    }

    public int getAccount() {
        return account;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public double adjustBalance(double adjustmentAmount){
        balance += adjustmentAmount;

        return balance;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof MasterRecord) {
            MasterRecord other = (MasterRecord) obj;

            return this.getAccount() == other.getAccount() &&
                    this.getName().equals(other.getName()) &&
                    this.getBalance() == other.getBalance();
        }
        return false;
    }

    @Override
    public String toString() {
        return "account #" + account + " (" + name + ") balance: " + balance;
    }
}

abstract class TransactionRecord {
    private int account;
    private int txNumber;
    private double balanceAdjustment;

    public TransactionRecord(int account, int txNumber, double balanceAdjustment) {
        this.account = account;
        this.txNumber = txNumber;
        this.balanceAdjustment = balanceAdjustment;
    }

    public double getBalanceAdjustment() {
        return balanceAdjustment;
    }
    public int getAccount() {
        return account;
    }
    public int getTxNumber() {
        return txNumber;
    }

    

    @Override
    public String toString() {
        return "ACC#" + account + "|" + balanceAdjustment;
    }
}

class PaymentRecord extends TransactionRecord {
    public PaymentRecord(int account, int txNumber, double paymentAmount) {
        super(account, txNumber, paymentAmount * -1);
    }

}

class OrderRecord extends TransactionRecord {
    String item;
    int quantity;
    double cost;

    public String getItemName() {
        return item;
    }

    public OrderRecord(int account, int txNumber, String item, int quantity, double cost) {
        super(account, txNumber, cost * quantity);
        this.item = item;
        this.quantity = quantity;
        this.cost = cost;
    }
}