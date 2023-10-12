import {Component} from '@angular/core';
import {ParkingLot} from "./ParkingLot";
import {ParkingLotService} from "./services/parking-lot.service";
import {HttpErrorResponse} from "@angular/common/http";
import {NgForm} from "@angular/forms";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'angularNew';
  public parkingLots!: ParkingLot[];
  public deleteParkingLot!: ParkingLot;
  public editParkingLot!: ParkingLot;
  public assignedParkingLot!: ParkingLot;
  public tap!: boolean;


  constructor(private service: ParkingLotService) {
  }

  ngOnInit() {
    this.tap = true;
    this.getParkingLots();
  }



  public getParkingLots() {
    this.service.getParkingLots().subscribe((resp) => {
      this.parkingLots = [];
      for(let i = 0, j = 0; i < resp.length; i++){
          if(resp[i].available === this.tap){
            this.parkingLots[j] = resp[i];
            j++;
          }
        }
      },
      (error: HttpErrorResponse) => {
        alert(error.message);
      })
  }

  onOpenModal(parkingLot: any, mode: string) {
    const container = document.getElementById('main-container');
    const button = document.createElement('button');
    button.type = 'button';
    button.style.display = 'none';
    button.setAttribute('data-toggle','modal')
    // let modes: {[name:string]: string} = {'add': '#addParkingLotModal', 'edit':'#updateParkingLotModal', etc....}
    // if(modes[mode] != null){
    //   button.setAttribute('data-target',modes[mode])
    //
    // }
    if(mode === 'add'){
      button.setAttribute('data-target','#addParkingLotModal')
    }
    if(mode === 'edit'){
      button.setAttribute('data-target','#updateParkingLotModal')
    }
    if(mode === 'delete'){
      this.deleteParkingLot = parkingLot;
      button.setAttribute('data-target','#deleteParkingLotModal')
    }
    if(mode === 'ava'){
      this.tap = true;
      this.getParkingLots();
    }
    if(mode === 'unava'){
      this.tap = false;
      this.getParkingLots();
    }
    if(mode === 'assign'){
      this.assignedParkingLot = parkingLot;
      button.setAttribute('data-target','#assignCarModal')
    }
    if(mode === 'available'){
      this.editParkingLot = parkingLot;
      button.setAttribute('data-target','#availableParkingLotModal')
    }
    if(container){
      container.appendChild(button);
    }
    button.click();
  }

  onAddParkingLot(addForm: NgForm) {
    // @ts-ignore
    document.getElementById('add-parkingLot-form').click();
    addForm.value.available = true;
    this.service.addParkingLot(addForm.value).subscribe(
      (response: ParkingLot) => {
        console.log(response);
        this.getParkingLots();
        addForm.reset();
      },
      (error: HttpErrorResponse) => {
        alert(error.message);
        addForm.reset();
      }
    );
  }

  public onDeleteParkingLot(id: string): void {
    this.service.removeParkingLot(id).subscribe((value)=>{
      console.log(value);
      this.getParkingLots();
    });
  }

  public searchParkingLot(key: string): void {
    const results: ParkingLot[] = [];
    for (const parkingLot of this.parkingLots) {
      if (parkingLot.name?.toLowerCase().indexOf(key.toLowerCase()) !== -1
        || (!this.tap && parkingLot.carNumber?.toLowerCase().indexOf(key.toLowerCase()) !== -1)) {
        results.push(parkingLot);
        console.log(parkingLot.name)
      }
    }
    this.parkingLots = results;
    if (results.length === 0 || !key) {
      this.getParkingLots();
    }
  }

  availableOrNot(parkingLot: ParkingLot) : string {
    return parkingLot.available ? './assets/ava.png' : './assets/unavailable.svg';
  }

  onAssignCar(assignForm: NgForm) {
    console.log(assignForm.value)
    assignForm.value.id = this.assignedParkingLot.id;
    assignForm.value.name = this.assignedParkingLot.name;
    assignForm.value.available = false;
    this.service.assignParkingLot(assignForm.value).subscribe(
      (response: ParkingLot) => {
        console.log(response);
        this.getParkingLots();
        assignForm.reset();
      },
      (error: HttpErrorResponse) => {
        alert(error.message);
        assignForm.reset();
      }
    );
  }
  onAvailable(parkingLot: ParkingLot){
    let pp: ParkingLot = {
      id: parkingLot.id,
      name: parkingLot.name,
      available: true
    }
    parkingLot = pp;
    // parkingLot['available'] = true;
    this.service.assignParkingLot(parkingLot).subscribe(
      (value)=>{
        console.log(value);
        this.getParkingLots()
      }
    )
  }
}
