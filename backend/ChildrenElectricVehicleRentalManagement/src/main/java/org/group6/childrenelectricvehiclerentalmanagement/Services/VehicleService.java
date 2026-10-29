package org.group6.childrenelectricvehiclerentalmanagement.Services;

import lombok.RequiredArgsConstructor;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.UpdateVehicleSlugRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.createVehicleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.UserException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.VehicleNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.VehicleSlugAlreadyExits;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    
    public Vehicle createNewVehicle(createVehicleRequest request) throws Exception {
        if(vehicleRepository.existsBySlug(request.vehicle_slug())) throw new VehicleSlugAlreadyExits();
        return vehicleRepository.save(new Vehicle(request.vehicle_slug()));
    }
    
    public List<Vehicle> getAllVehicle() {
        return vehicleRepository.findAll();
    }

    public Vehicle getVehicle(long id) throws Exception {
        return vehicleRepository.findById(id).orElseThrow(VehicleNotExits::new);
    }

    public Vehicle updateVehicleSlug(long id, UpdateVehicleSlugRequest request) throws Exception{
        Vehicle targetVehicel = this.getVehicle(id);
        switch(targetVehicel.getStatus()){
            case Inused -> throw new UserException("vehicle is using","Không được đổi tên khi xe đang sử dụng");
            case Online -> throw new UserException("vehicle is waiting to be use","Không được đổi tên khi xe đang trong hàng chờ");
            case Repairing -> throw new UserException("vehicle is repairing","Xe đang được sửa chữa");
            case Deleted -> throw new UserException("vehicle already deleted","Xe đã bị xóa");
        }

        targetVehicel.setSlug(request.vehicle_slug());
        return vehicleRepository.save(targetVehicel);
    }

    public Vehicle updateVehicleStatus(long id, VehicleStatus newStatus) throws Exception{
        Vehicle targetVehicel = this.getVehicle(id);

        if (newStatus.equals(VehicleStatus.Deleted)) throw new UserException("this api is not allow for deleting vehicle","Lỗi phần mềm, vui lòng liên hệ quản trị");

        switch (targetVehicel.getStatus()){
            case Online -> {
                switch (newStatus){
                    case Online -> {
                        throw new UserException("vehicle is in the same status", "Xe đang ở trạng thái này");
                    }
                    case Deleted -> {
                        throw new UserException("vehicle status should be offline to be delete","Phải chuyển xe sang trạng thái offline mới được xóa");
                    }
                }
            }
            case Offline -> {
                switch (newStatus){
                    case Offline -> throw new UserException("vehicle is in the same status","Xe đang ở trạng thái này");
                    case Inused -> throw new UserException("vehicle is not ready to be using","Xe phải ở trạng thái chờ mới có thể sử dụng");
                }
            }
            case Inused -> {
                switch (newStatus){
                    case Inused -> throw new UserException("vehicle is in the same status","Xe đang ở trạng thái này");
                    case Deleted -> throw new UserException("vehicle must be offline to be delete","Không được xóa xe khi đang sử dụng");
                }
            }
            case Deleted -> throw new UserException("the vehicle is deleted, updating is not allow","Xe đã bị xóa");
            case Repairing -> {
                switch (newStatus){
                    case Repairing -> throw new UserException("vehicle is in the same status","Xe đang ở trạng thái này");
                    case Inused -> throw new UserException("vehicle is not avaliable to be using","Đưa xe vào trạng thái chờ để có thể dùng");
                }
            }
        }
        targetVehicel.setStatus(newStatus);

        return vehicleRepository.save(targetVehicel);
    }

    public void deleteVehicle(long id) throws Exception {
        Vehicle target = this.getVehicle(id);
        switch (target.getStatus()){
            case Deleted -> throw new UserException("vehicle is already deleted","Xe đã bị xóa từ trước");
            case Inused -> throw new UserException("vehicle is being use","Không thể xóa khi xe đang được sử dụng");
            case Online -> throw new UserException("vehicle is online","Không thể xóa khi xe đang trong trạng thái chờ");
        }
    }
}
