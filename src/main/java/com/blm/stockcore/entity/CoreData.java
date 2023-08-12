package com.blm.stockcore.entity;

public class CoreData{
        public int total;
        CoreStock[] diff;

        public CoreStock[] getDiff() {
            return diff;
        }

        public void setDiff(CoreStock[] diff) {
            this.diff = diff;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }


    }
