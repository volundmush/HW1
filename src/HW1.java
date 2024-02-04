/*
 * Author:  Andrew Bastien
 * Email: abastien2021@my.fit.edu
 * Course:  CSE 2010
 * Section: E4
 * Term: Spring 2024
 * Project: HW1, SinglyLinkedLists
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

public class HW1 {
   private final Scanner data;
   private static class Entry {
      public String seller;
      public String product;
      public double price;
      public double shippingCost;
      public int quantity;

      public Entry(String seller, String product, double price, double shippingCost, int quantity) {
         this.seller = seller;
         this.product = product;
         this.price = price;
         this.shippingCost = shippingCost;
         this.quantity = quantity;
      }

      public double getTotalCost() {
         return price + shippingCost;
      }

      public String renderAddSeller() {
         if(quantity <= 0) {
            return String.format("AddSeller %s %s %.2f %.2f %d NonPositiveQuantityError", product, seller, price, shippingCost, 0);
         }
         return String.format("AddSeller %s %s %.2f %.2f %d", product, seller, price, shippingCost, quantity);
      }

      public String renderTableLine() {
         return String.format("%10s %13.2f %13.2f %10.2f", seller, price, shippingCost, getTotalCost());
      }
   }

   private final SinglyLinkedList<Entry> entries = new SinglyLinkedList<>();
   private final SinglyLinkedList<String> output = new SinglyLinkedList<>();

   public HW1(Scanner data) {
      this.data = data;
   }

   private void handleAddSeller() {
      // Input: AddSeller product seller price shippingCost quantity
      // Output: AddSeller product seller price shippingCost quantity [NonPositiveQuantityError]
      final String product = data.next();
      final String seller = data.next();
      final double price = data.nextDouble();
      final double shippingCost = data.nextDouble();
      final int quantity = data.nextInt();

      final Entry newEntry = new Entry(seller, product, price, shippingCost, quantity);

      final double totalCost = newEntry.getTotalCost();

      if(quantity <= 0) {
         // TODO: print error and return...
         output.addLast(newEntry.renderAddSeller());
         return;
      }

      // If there are no entries, this is the simplest case. just add it.
      if(entries.isEmpty()) {
         entries.addFirst(newEntry);
         // TODO: Print head.
         output.addLast(newEntry.renderAddSeller());
         return;
      }

      // The second case is, if this should replace the head.
      if(entries.head.getElement().product.equals(newEntry.product)) {
         if(entries.head.getElement().getTotalCost() > totalCost) {
            // We have a new head.
            entries.addFirst(newEntry);
            output.addLast(newEntry.renderAddSeller());
            return;
         }
      }

      // AddSeller will always enter SOMETHING.
      SinglyLinkedList.Node<Entry> current = entries.head;

      while(true) {
         Entry curElem = current.getElement();
         if(curElem.product.equals(product)) {
            // we have a match. Now we must price compare.
            final double curTotal = curElem.getTotalCost();
            if(totalCost < curTotal) {
               //  the simplest case. we insert newEntry before current.
               entries.addBefore(newEntry, current);
               output.addLast(newEntry.renderAddSeller());
               return;
            }
            if(totalCost == curTotal) {
               // we must compare the seller names alphabetically and it will determine if the new should go
               // before or after.
               if(seller.compareTo(curElem.seller) < 0) {
                  entries.addBefore(newEntry, current);
               } else {
                  entries.addAfter(newEntry, current);
               }
               output.addLast(newEntry.renderAddSeller());
               return;
            }
         }
         if(current.getNext() == null) {
            // We have reached the tail.
            entries.addLast(newEntry);
            output.addLast(newEntry.renderAddSeller());
            return;
         }
         // Advance the iterator.
         current = current.getNext();
      }
   }

   private Boolean handleRemoveSellerHelper(String product, String seller) {
      SinglyLinkedList.Node<Entry> current = entries.head;
      if(current == null) return false;

      // Simplest case: match is the head.
      Entry en = current.getElement();
      if(en.seller.equals(seller) && en.product.equals(product)) {
         entries.removeFirst();
         return true;
      }

      // need to walk the list..
      while(current != null) {
         SinglyLinkedList.Node<Entry> next = current.getNext();
         if(next == null) return false;
         en = next.getElement();
         if(en.seller.equals(seller) && en.product.equals(product)) {
            // We have a match!
            if(next == entries.tail) {
               entries.tail = current;
               current.setNext(null);
               return true;
            }
            // it's not the tail... and not the head.
            current.setNext(next.getNext());
            next.setNext(null);
            // The garbage collector should deal with 'next' now.
            return true;
         }
         current = current.getNext();
      }
      return false;
   }

   private void handleRemoveSeller() {
      // Input: RemoveSeller product seller
      // Output: RemoveSeller product seller [NonExistingSellerError]
      final String product = data.next();
      final String seller = data.next();
      if(handleRemoveSellerHelper(product, seller)) {
         output.addLast(String.format("RemoveSeller %s %s", product, seller));
      } else {
         output.addLast(String.format("RemoveSeller %s %s NonExistingSellerError", product, seller));
      }
   }

   /* Example output for handleDisplaySellerlist

   12345678901234567890123456789012345678901234567890 // just to show spacing
   seller productPrice shippingCost totalCost // output starts
   walmart 20.99 0.00 20.99
   amazon 16.95 5.00 21.95
   bestbuy 21.99 0.00 21.99

    */

   private void handleDisplaySellerList() {
      // Input: DisplaySellerList product
      final String product = data.next();
      output.addLast(String.format("DisplaySellerList %s", product));
      output.addLast(String.format("%10s%14s%14s%11s", "seller", "productPrice", "shippingCost", "totalCost"));

      SinglyLinkedList.Node<Entry> current = entries.head;
      while(current != null) {
         Entry en = current.getElement();
         if(en.product.equals(product)) {
            output.addLast(en.renderTableLine());
         }
         current = current.getNext();
      }

   }

   private void handleIncreaseInventory() {
      // Input: IncreaseInventory product seller quantity
      // Output: IncreaseInventory product seller quantity updatedInventory
      final String product = data.next();
      final String seller = data.next();
      final int quantity = data.nextInt();
      if(quantity <= 0) {
         output.addLast(String.format("IncreaseInventory %s %s %d NonPositiveQuantityError", product, seller, 0));
         return;
      }

      SinglyLinkedList.Node<Entry> current = entries.head;
      while(current != null) {
         Entry en = current.getElement();
         if(en.seller.equals(seller) && en.product.equals(product)) {
            en.quantity += quantity;
            output.addLast(String.format("IncreaseInventory %s %s %d", product, seller, en.quantity));
            return;
         }
         current = current.getNext();
      }
      output.addLast(String.format("IncreaseInventory %s %s SellerProductNotFoundError", product, seller));

   }

   private void handleCustomerPurchase() {
      // Input: CustomerPurchase product seller quantity
      // Output: CustomerPurchase product seller quantity updatedInventory or NotEnoughInventoryError
      // OR output: DepletedInventoryRemoveSeller product seller
      final String product = data.next();
      final String seller = data.next();
      final int quantity = data.nextInt();

      SinglyLinkedList.Node<Entry> current = entries.head;
      while(current != null) {
         Entry en = current.getElement();
         if(en.seller.equals(seller) && en.product.equals(product)) {
            if(quantity > en.quantity) {
               output.addLast(String.format("CustomerPurchase %s %s %d NotEnoughInventoryError", product, seller, quantity));
               return;
            }
            en.quantity -= quantity;
            output.addLast(String.format("CustomerPurchase %s %s %d %d", product, seller, quantity, en.quantity));
            if(en.quantity == 0) {
               handleRemoveSellerHelper(product, seller);
               output.addLast(String.format("DepletedInventoryRemoveSeller %s %s", product, seller));
            }
            return;
         }
         current = current.getNext();
      }

   }

   private void handleLine () {
      // Every line begins with 'command' and 'product'.
      final String command = data.next();

      // Simple if-else tree to deal with commands...
      if (command.equals("AddSeller")) {
         handleAddSeller();
      } else if (command.equals("RemoveSeller")) {
         handleRemoveSeller();
      } else if (command.equals("DisplaySellerList")) {
         handleDisplaySellerList();
      } else if (command.equals("IncreaseInventory")) {
         handleIncreaseInventory();
      } else if (command.equals("CustomerPurchase")) {
         handleCustomerPurchase();
      }
   }

   public void run() {
      while (data.hasNext()) {
         handleLine();
      }
      SinglyLinkedList.Node<String> current = output.head;
      while(current != null) {
         System.out.println(current.getElement());
         current = current.getNext();
      }
   }

   public static void main(final String[] args) {
      // Let's check for the given file?
      if (args.length < 1) {
         System.out.println("No file path provided.");
         return;
      }

      // use java.util.Scanner because dang this is complicated.
      try {
         File file = new File(args[0]);
         Scanner data = new Scanner(file, StandardCharsets.US_ASCII.name());
         HW1 program = new HW1(data);
         program.run();
      } catch (FileNotFoundException e) {
         System.out.println("File not found: " + args[0]);
         return;
      }

   }

}



